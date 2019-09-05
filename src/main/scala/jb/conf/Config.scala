package jb.conf

import jb.model.Cube
import jb.util.functions.WeightAggregators

object Config {

  /** Models */
  val maxDepth: Int = 3
  val impurity = "gini"

  /** Mapping */
  val weightingFunction: Array[Cube] => Double = WeightAggregators.sumOfVolumes

  /** Result catcher */
  val treshold: Double = .3
  val batch: Int = 2
  val minIter: Int = 20
  val maxIter: Int = Int.MaxValue
  val numberOfReferences: Int = 3

}
