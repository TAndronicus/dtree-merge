package jb.model

case class Measurements(
                         acc: Double,
                         precision: Double,
                         recall: Double,
                         fscore: Double,
                         specificity: Double,
                         auc: Double
                       ) {

  def toArray: Array[Double] = Array(
    acc, precision, recall, fscore, specificity, acc
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

  def integratedQuality(array: Array[Array[Double]]): Double = array
    .map(a => if (a(2 * numberOfMetrics) > a(numberOfMetrics) || a(2 * numberOfMetrics) > a(0)) 1 else 0)
    .sum.toDouble / array.length

}
