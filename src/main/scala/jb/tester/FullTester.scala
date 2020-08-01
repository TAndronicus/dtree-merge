package jb.tester

import jb.conf.Config
import jb.model.{Cube, Measurements}
import jb.util.Const.{FEATURES, LABEL, PREDICTION}
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.linalg.{DenseVector, SparseVector}
import org.apache.spark.sql.DataFrame

object FullTester {


  def testMv(testSubset: DataFrame, nClassif: Int): Measurements = {
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

  def testWMv(testSubset: DataFrame, nClassif: Int, rects: Array[Array[Cube]], weightingFunction: Array[Cube] => Double): Measurements = {
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

  def testI(predictions: Array[Double], testSubset: DataFrame): Measurements = {
    val refLabels = getReferenceLabels(testSubset)
    calculateStatistics(predictions, refLabels)
  }

  private def getReferenceLabels(testedSubset: DataFrame): Array[Double] = {
    testedSubset.select(LABEL).collect().map(_.get(0)).map {
      case int: Int => int.toDouble
      case double: Double => double
    }
  }

  private def calculateStatistics(predLabels: Array[Double], refLabels: Array[Double]): Measurements = {
    val allIndexesSize = refLabels.length.toDouble
    val allLabels = refLabels.distinct
    val indexMap = allLabels.map(
      label => (
        refLabels.zipWithIndex.collect { case (l, i) if l == label => i }.toSet,
        predLabels.zipWithIndex.collect { case (l, i) if l == label => i }.toSet
      )
    )
    val acc = indexMap
      .map { case (ref, pred) => (allIndexesSize - ((ref ++ pred).size - (ref & pred).size)) / allIndexesSize }
      .sum / allLabels.length
    val precissionMi = indexMap
      .map { case (ref, pred) => (ref & pred).size }
      .sum.toDouble / indexMap
      .map { case (_, pred) => pred.size }
      .sum
    val recallMi = indexMap
      .map { case (ref, pred) => (ref & pred).size }
      .sum.toDouble / indexMap
      .map { case (ref, _) => ref.size }
      .sum
    val precissionM = indexMap
      .map { case (ref, pred) => (ref & pred).size.toDouble / pred.size }
      .filter(!_.isNaN)
      .sum / allLabels.length
    val recallM = indexMap
      .map { case (ref, pred) => (ref & pred).size.toDouble / ref.size }
      .filter(!_.isNaN)
      .sum / allLabels.length
    Measurements(acc,
      precissionMi, recallMi, fScore(precissionMi, recallMi, 1), // TODO: hardcoded beta
      precissionM, recallM, fScore(precissionM, recallM, 1)
    )
  }

  def fScore(precission: Double, recall: Double, beta: Double): Double = (math.pow(beta, 2) + 1) * precission * recall / (math.pow(beta, 2) * precission + recall)

  def testRF(trainingSubset: DataFrame, testSubset: DataFrame, nClassif: Int): Measurements = {
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
