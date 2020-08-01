package jb.model

case class Measurements(
                         measurementType: MeasurementType,
                         mv: Double,
                         rf: Double,
                         wMvs: Array[Double],
                         i: Array[Double]
                       )
