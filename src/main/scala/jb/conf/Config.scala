package jb.conf

import jb.model.Cube
import jb.util.functions.WeightAggregators

object Config {

  /** Models */
  val maxDepth: Int = 5
  val impurity = "gini"
  val nFeatures = 2
  val nClassifs = Array(5)
  val divisions = Array(20, 40, 60)
  val numberOfDisplacements = 5

  /** Mapping */
  val weightingFunctions: Array[Array[Cube] => Double] = Array(
    WeightAggregators.sumOfVolumes,
    WeightAggregators.sumOfVolumesInv,
  )

  /** Result catcher */
  val treshold: Double = .4
  val batch: Int = 4
  val minIter: Int = 10
  val maxIter: Int = 200

  /** Other */
  val joinTrainingAndValidationSet = true

}
