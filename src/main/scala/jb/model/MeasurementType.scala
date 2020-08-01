package jb.model

sealed class MeasurementType(val priority: Int)

case class Acc() extends MeasurementType(0)

case class Mcc() extends MeasurementType(1)
