package jb.conf

import jb.model.Cube
import jb.util.functions.WeightAggregators

object Config {

  /** Models */
  val maxDepth: Int = 5
  val impurity = "gini"
  val nFeatures = 2
  val nClassifs: Array[Int] = Array(5)
  val divisions: Array[Int] = Array(20, 40, 60)
  val numberOfDisplacements = 2
  val referenceAlgorithms: Array[String] = Array("MV", "RF")
  val weightedReferenceAlgorithms: Array[String] = Array("wMV")

  /** Mapping */
  val weightingFunctions: Array[Array[Cube] => Double] = Array(
    WeightAggregators.sumOfVolumes,
    WeightAggregators.sumOfVolumesInv,
  )

  /** Result catcher */
  val treshold: Double = .25
  val batch: Int = 4
  val minIter: Int = 10
  val maxIter: Int = 200

  /** Other */
  val joinTrainingAndValidationSet = true

}
