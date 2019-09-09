package jb.parser

import jb.model.{Cube, InternalSimpleNode, LeafSimpleNode}
import jb.util.functions.WeightAggregators._
import jb.util.functions.WithinDeterminers._
import org.scalatest.FunSuite

class TreeParserTest extends FunSuite{

  test("labelCalculation sumOfValues") {
    // given
    val mins = Array(0D, 0D)
    val maxes = Array(2D, 2D)
    val rects = Array(
      Array(
        Cube(Array(0, 0), Array(1, 1)), // is within, 0
        Cube(Array(0, 1), Array(3, 2)), // is within, 0
        Cube(Array(0, 0), Array(2, 2), 1) // is within, 1
      ),
      Array(
        Cube(Array(0, 0), Array(2, 2)), //is within, 0
        Cube(Array(0, 0), Array(2, 2)), //is within, 0
        Cube(Array(1.5, 0), Array(2.5, 2), 1) // is not within 1
      ),
      Array(
        Cube(Array(0, 0), Array(2, 2), 1)
      )
    )
    val treeParser = new TreeParser(spansMid)
    val res = treeParser.calculateLabel(sumOfVolumes, mins, maxes, rects)
    assert(res == 0D)
  }

  test("rect2dt") {
    // given
    val mins = Array(0D, 0D)
    val maxes = Array(5D, 3D)
    val division = 5
    val elSizes = Array(1D, 1D)
    val rects = Array(
      Array(
        Cube(Array(0, 0), Array(3, 1)),
        Cube(Array(0, 1), Array(3, 3), 1),
        Cube(Array(3, 0), Array(5, 1)),
        Cube(Array(3, 1), Array(5, 3), 1)
      )
    )

    // when
    val treeParser = new TreeParser(spansMid)
    implicit val mapper: Array[Cube] => Double = sumOfVolumes
    val tree = treeParser.rect2dt(mins, maxes, elSizes, 0, 2, rects)

    // then
    // tree values
    assert(tree.asInstanceOf[InternalSimpleNode].split.value == 2)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.value == 1)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.featureIndex == 1)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.value == 1)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[LeafSimpleNode].label == 0)
    assert(tree.asInstanceOf[InternalSimpleNode].rightChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].rightChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[LeafSimpleNode].label == 1)
  }

}
