package jb.tester

import jb.conf.Config
import jb.model.Cube
import jb.server.SparkEmbedded
import jb.util.Const.{FEATURES, LABEL, PREDICTION}
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.linalg.{DenseVector, SparseVector}
import org.apache.spark.sql.DataFrame
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import jb.server.SparkEmbedded.ss.sqlContext.implicits._

object FullTester {


  def testMv(testSubset: DataFrame, nClassif: Int): (Double, Double) = {
    val cols = for (i <- 0.until(nClassif)) yield PREDICTION + "_" + i
    val mvLabels = testSubset.select(cols.head, cols.takeRight(cols.length - 1): _*).collect() // Arrays with predictions for every clf
      .map(row => row.toSeq // array of predictions
        .groupBy(_.asInstanceOf[Double].doubleValue()) // map label -> array of label repetitions
        .mapValues(_.length) // map label -> count
        .reduce((t1, t2) => if (t1._2 > t2._2) t1 else t2)) // tuple most frequent label -> count
      .map(_._1) // most frequent labels
    val refLabels = getReferenceLabels(testSubset)
    calculateStatistics(mvLabels, refLabels)
  }

  def getWeightFromRects(rects: Array[Array[Cube]], index: Int, value: Any, weightingFunction: Array[Cube] => Double): Double = {
    value match {
      case s: SparseVector => weightingFunction(rects(index).filter(cube => cube.contains(s.toArray)))
      case d: DenseVector => weightingFunction(rects(index).filter(cube => cube.contains(d.toArray)))
    }
  }

  def testWMv(testSubset: DataFrame, nClassif: Int, rects: Array[Array[Cube]], weightingFunction: Array[Cube] => Double): (Double, Double) = {
    val cols = for (i <- 0.until(nClassif)) yield PREDICTION + "_" + i
    val mvLabels = testSubset.select(FEATURES, cols: _*).collect() // Arrays with features values and predictions for every clf [features: SparseVector, prediction_0, prediction_1, ..., prediction_n]
      .map(row => row.toSeq.tail.indices.map(index => (row.get(index + 1).asInstanceOf[Double].doubleValue(), getWeightFromRects(rects, index, row.get(0), weightingFunction))) // array of predictions - weights tuples
        .groupBy(_._1) // map label -> array of label repetitions
        .mapValues(_.map(_._2).sum) // map label -> sum of weights
        .reduce((t1, t2) => if (t1._2 > t2._2) t1 else t2)) // tuple label wight greatest overall weight -> overall weight
      .map(_._1) // label with greatest weight
    val refLabels = getReferenceLabels(testSubset)
    calculateStatistics(mvLabels, refLabels)
  }

  def testI(predictions: Array[Double], testSubset: DataFrame): (Double, Double) = {
    val refLabels = getReferenceLabels(testSubset)
    calculateStatistics(predictions, refLabels)
  }

  private def getReferenceLabels(testedSubset: DataFrame): Array[Double] = {
    testedSubset.select(LABEL).collect().map(_.get(0)).map {
      case int: Int => int.toDouble
      case double: Double => double
    }
  }

  private def calculateStatistics(predLabels: Array[Double], refLabels: Array[Double]): (Double, Double) = {
//    val metrics = new BinaryClassificationMetrics(SparkEmbedded.ss.sparkContext.parallelize(predLabels.zip(refLabels)))
//    println(metrics.precisionByThreshold().collect().map(_._2).max)
//    println(metrics.areaUnderROC())
//    println(metrics.areaUnderPR())
//    metrics.unpersist()
    val matched = predLabels.indices.map(i => (predLabels(i), refLabels(i))).groupBy(identity).mapValues(_.size)
    val (tp, tn, fp, fn) = (matched.getOrElse((1, 1), 0), matched.getOrElse((0, 0), 0), matched.getOrElse((1, 0), 0), matched.getOrElse((0, 1), 0))
    ((tp + tn).toDouble / (tp + tn + fp + fn),
      (tp * tn - fp * fn).toDouble / math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)))
  }

  def testRF(trainingSubset: DataFrame, testSubset: DataFrame, nClassif: Int): (Double, Double) = {
    trainingSubset.cache()
    val predictions = new RandomForestClassifier()
      .setFeatureSubsetStrategy("auto")
      .setImpurity(Config.impurity)
      .setNumTrees(nClassif)
      .setMaxDepth(Config.maxDepth)
      .setFeaturesCol(FEATURES)
      .setLabelCol(LABEL)
      .fit(trainingSubset)
      .transform(testSubset)
      .select(PREDICTION)
      .collect()
      .toSeq
      .map(a => a.get(0).asInstanceOf[Double])
      .toArray
    trainingSubset.unpersist()
    val reference = getReferenceLabels(testSubset)
    calculateStatistics(predictions, reference)
  }

}
