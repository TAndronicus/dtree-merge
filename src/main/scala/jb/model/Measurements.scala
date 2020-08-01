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

  def +(other: Measurements): Measurements = Measurements(
    acc + other.acc,
    precissionMi + other.precissionMi,
    recallMi + other.recallMi,
    fScoreMi + other.fScoreMi,
    precissionM + other.precissionM,
    recallM + other.recallM,
    fScoreM + other.fScoreM
  )

  def /(divisor: Double): Measurements = Measurements(
    acc / divisor,
    precissionMi / divisor,
    recallMi / divisor,
    fScoreMi / divisor,
    precissionM / divisor,
    recallM / divisor,
    fScoreM / divisor
  )

}
