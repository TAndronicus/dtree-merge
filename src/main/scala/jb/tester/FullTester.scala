package jb.tester

import jb.conf.Config
import jb.util.Const.{FEATURES, LABEL, PREDICTION}
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.sql.DataFrame

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
