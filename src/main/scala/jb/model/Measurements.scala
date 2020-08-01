package jb.model

case class Measurements(
                         acc: Double,
                         precissionMi: Double,
                         recallMi: Double,
                         fScoreMi: Double,
                         precissionM: Double,
                         recallM: Double,
                         fScoreM: Double
                       ) {
  def toArray: Array[Double] = Array(
    acc: Double,
    precissionMi: Double,
    recallMi: Double,
    fScoreMi: Double,
    precissionM: Double,
    recallM: Double,
    fScoreM: Double
  )
}
