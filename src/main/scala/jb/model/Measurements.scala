package jb.model

case class Measurements(
                         acc: Double,
                         precision: Double,
                         recall: Double,
                         fscore: Double,
                         specificity: Double,
                         auc: Double
                       )

object Measurements {
  val numberOfMetrics: Int = 6

  def integratedQuality(array: Array[Array[Double]]): Double = array
    .map(a => if (a(2 * numberOfMetrics) > a(numberOfMetrics) || a(2 * numberOfMetrics) > a(0)) 1 else 0)
    .sum.toDouble / array.length

}
