package jb.model

import jb.util.Util.withRequirement

case class Measurements(
                         acc: Double,
                         precision: Double,
                         recall: Double,
                         fscore: Double,
                         specificity: Double,
                         auc: Double
                       ) {

  def toArray: Array[Double] =
    withRequirement[Array[Double]](
      Array(acc, precision, recall, fscore, specificity, auc),
      _.length == Measurements.numberOfMetrics
    )


  def +(other: Measurements): Measurements = Measurements(
    acc + other.acc,
    precision + other.precision,
    recall + other.recall,
    fscore + other.fscore,
    specificity + other.specificity,
    auc + other.auc
  )

  def /(divisor: Double): Measurements = Measurements(
    acc / divisor,
    precision / divisor,
    recall / divisor,
    fscore / divisor,
    specificity / divisor,
    auc / divisor
  )

}

object Measurements {
  val numberOfMetrics: Int = 6

  def integratedQuality(array: Array[Array[Double]]): Double = {
    array
      .map(integratedQualityValue)
      .sum.toDouble / array.length
  }

  def integratedQualityValue(array: Array[Double]): Int = {
    if ((array(4 * numberOfMetrics) > array(0) && array(4 * numberOfMetrics) > array(numberOfMetrics))
      || (array(5 * numberOfMetrics) > array(0) && array(5 * numberOfMetrics) > array(numberOfMetrics))
      || (array(6 * numberOfMetrics) > array(0) && array(6 * numberOfMetrics) > array(numberOfMetrics))
      || (array(7 * numberOfMetrics) > array(0) && array(7 * numberOfMetrics) > array(numberOfMetrics))
      || (array(8 * numberOfMetrics) > array(0) && array(8 * numberOfMetrics) > array(numberOfMetrics))
      || (array(9 * numberOfMetrics) > array(0) && array(9 * numberOfMetrics) > array(numberOfMetrics))) 1 else 0
  }

  def names: Array[String] = {
    withRequirement[Array[String]](
      Array("acc", "precision", "recall", "fscore", "specificity", "auc"),
      _.length == numberOfMetrics
    )
  }

}
