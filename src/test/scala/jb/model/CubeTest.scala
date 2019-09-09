package jb.model

import jb.util.functions.WithinDeterminers._
import org.scalatest.FunSuite

class CubeTest extends FunSuite{

  test("Rect creation") {
    val rect = Cube(Array(0D), Array(1.5))
    assert(rect.mid(0) == 0.75)
  }

  test("Rect redefinition") {
    val rect = Cube(Array(0D), Array(1.5))
    rect.min = Array(0.5)
    assert(rect.mid(0) == 1)
  }

  test("Rect volume") {
    val rect = Cube(Array(0, 1, 2.5), Array(1, 3, 6.5))
    assert(rect.volume == 8)
  }

  test("Is within") {
    val rect = Cube(Array(-1, -1), Array(1, 1))
    assert(rect.isWithin(spansMid(Array(-.2, -.3), Array(.4, .5))))
    assert(rect.isWithin(spansMid(Array(-2, -3), Array(3, 2))))
    assert(rect.isWithin(spansMid(Array(0, -2), Array(-2, 0))))
    assert(!rect.isWithin(spansMid(Array(1.5, 0), Array(2.5, 1))))
  }

}
