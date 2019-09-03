package jb.tester

import jb.model.Cube
import jb.util.functions.WeightAggregators
import org.apache.spark.ml.linalg.DenseVector
import org.scalatest.FunSuite

class FullTesterTest extends FunSuite {

  test("should get volume as weight"){
    // given
    val point = new DenseVector(Array(1.5, 1.5))
    val rects = Array(
      Array(
        Cube(Array(0, 0), Array(1, 1)), // is within, 0
        Cube(Array(0, 1), Array(3, 2)), // is within, 0
        Cube(Array(1, 0), Array(3, 1), 1) // is within, 1
      ),
      Array(
        Cube(Array(0, 0), Array(1, 2)), //is within, 0
        Cube(Array(1, 0), Array(3, 1)), //is within, 0
        Cube(Array(1, 1), Array(3, 2), 1) // is not within 1
      ),
      Array(
        Cube(Array(0, 0), Array(3, 2), 1)
      ),
      Array(
        Cube(Array(0, 0), Array(1.375, 2)),
        Cube(Array(1.375, 0), Array(1.625, 2), 1),
        Cube(Array(1.625, 0), Array(3, 2))
      )
    )

    // when
    val firstProportional = FullTester.getWeightFromRects(rects, 0, point, WeightAggregators.sumOfVolumes)
    val secondProportional = FullTester.getWeightFromRects(rects, 1, point, WeightAggregators.sumOfVolumes)
    val thirdInverse = FullTester.getWeightFromRects(rects, 2, point, WeightAggregators.sumOfVolumesInv)
    val fourthInverse = FullTester.getWeightFromRects(rects, 3, point, WeightAggregators.sumOfVolumesInv)

    // then
    assert(firstProportional == 3)
    assert(secondProportional == 2)
    assert(thirdInverse == 1d / 6)
    assert(fourthInverse == 2)
  }

}
