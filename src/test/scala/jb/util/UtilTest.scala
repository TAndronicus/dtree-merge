package jb.util

import jb.conf.Config
import jb.server.SparkEmbedded
import jb.util.Util._
import org.scalatest.FunSuite

import scala.collection.mutable.ArrayBuffer

class UtilTest extends FunSuite {

  test("should conduct proper aggregation") {
    // given
    val container = Array(
      Array(
        Array(.5, .7, .9),
        Array(.1, .4, .7)
      ),
      Array(
        Array(.3, .5, .9),
        Array(.1, .2, .5)
      ),
      Array(
        Array(.4, .3, .3),
        Array(.4, .6, .3)
      ),
      Array(
        Array(.4, .1, .1),
        Array(0D, .4, .5)
      )
    )

    // when
    val result = testAggregate(container)

    // then
    assert(result.length == 2)
    result.map(_.length == 3).foreach(assert(_))
    val delta = 1e-6
    val expectedResult = Array(
      Array(.4, .4, .55),
      Array(.15, .4, .5)
    )
    result.indices.foreach(
      outerIndex => result(0).indices.map(
        innerIndex => result(outerIndex)(innerIndex) - expectedResult(outerIndex)(innerIndex)
      ).map(math.abs).map(_ < delta).foreach(assert(_))
    )
  }

  test("displacement without step") {
    // given
    val expectedDivision = Array(0, .2, .4, .6, .8, 1)

    // when
    val result = Util.getDimensionDivision(5, 0)

    // then
    assert(expectedDivision.length == result.length)
    expectedDivision.indices.foreach(index => assert(math.abs(expectedDivision(index) - result(index)) < 10e-6))
  }

  test("displacement with step") {
    // given
    val divisions = 5
    val step = 3
    val withoutStep = Array(0, .2, .4, .6, .8, 1)
    val buffer = ArrayBuffer(0d)
    withoutStep.take(withoutStep.length - 1).foreach(el => buffer += el + step * 1d / (Config.numberOfDisplacements * divisions))
    buffer += 1
    val expectedDivision = buffer.toArray

    // when
    val result = Util.getDimensionDivision(divisions, step)

    // then
    assert(expectedDivision.length == result.length)
    expectedDivision.indices.foreach(index => assert(math.abs(expectedDivision(index) - result(index)) < 10e-6))
  }

  test("displacement setup") {
    // given
    val divisions = 5
    val yStep = 3
    val expectedXDivision = Array(0, .2, .4, .6, .8, 1)
    val expectedXIndices = Array(0, 1, 2, 3, 4, 5)
    val withoutStep = Array(0, .2, .4, .6, .8, 1)
    val buffer = ArrayBuffer(0d)
    withoutStep.take(withoutStep.length - 1).foreach(el => buffer += el + yStep * 1d / (Config.numberOfDisplacements * divisions))
    buffer += 1
    val expectedYDivision = buffer.toArray
    val expectedYIndices = Array(0, 1, 2, 3, 4, 5, 6)

    // when
    val (xId, yId, split) = Util.getDisplacementSetup(divisions, 0, yStep)

    // then
    assert(xId.length == expectedXIndices.length)
    xId.indices.foreach(index => assert(xId(index) == expectedXIndices(index)))
    split(0).indices.foreach(index => assert(math.abs(split(0)(index) - expectedXDivision(index)) < 10e-6))
    assert(yId.length == expectedYIndices.length)
    yId.indices.foreach(index => assert(yId(index) == expectedYIndices(index)))
    split(1).indices.foreach(index => assert(math.abs(split(1)(index) - expectedYDivision(index)) < 10e-6))
  }

}
