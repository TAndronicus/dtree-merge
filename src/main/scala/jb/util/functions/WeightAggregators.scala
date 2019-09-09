package jb.util.functions

import jb.model.Cube

object WeightAggregators {

  val noOp: Array[Cube] => Double = _ => 1D;

  val sumOfVolumes: Array[Cube] => Double = (ar: Array[Cube]) => ar.map(_.volume).sum

  val sumOfVolumesInv: Array[Cube] => Double = (ar: Array[Cube]) => ar.map(el => 1 / el.volume).sum

  val names: Map[Array[Cube] => Double, String] = Map(
    noOp -> "noOp",
    sumOfVolumes -> "vol",
    sumOfVolumesInv -> "inv"
  )

}
