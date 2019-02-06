package jb.util

import org.scalatest.FunSuite
import jb.util.Util._

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
    result.map(_.length == 2).foreach(assert)
    val delta = 1e-6
    val expectedResult = Array(
      Array(.4, .4, .55),
      Array(.15, .4, .5)
    )
    result.indices.foreach(
      outerIndex => result(0).indices.map(
        innerIndex => result(outerIndex)(innerIndex) - expectedResult(outerIndex)(innerIndex)
      ).map(math.abs).map(_ < delta).foreach(assert)
    )
  }

}
