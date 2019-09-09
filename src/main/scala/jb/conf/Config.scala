package jb.conf

import jb.model.Cube
import jb.util.functions.WeightAggregators

object Config {

  /** Models */
  val maxDepth: Int = 3
  val impurity = "gini"

  /** Mapping */
  val weightingFunctions: Array[Array[Cube] => Double] = Array(
    WeightAggregators.sumOfVolumes,
    WeightAggregators.sumOfVolumesInv,
  )

  /** Result catcher */
  val treshold: Double = .1
  val batch: Int = 1
  val minIter: Int = 1
  val maxIter: Int = Int.MaxValue
  val numberOfReferences: Int = 3

}
