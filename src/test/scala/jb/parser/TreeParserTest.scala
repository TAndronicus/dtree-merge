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

  test("rect2dtWithoutOffset") {
    // given
    val xDivision = 5
    val yDivision = 3
    val (mins, maxes) = (Array(0, 0), Array(xDivision, yDivision))
    val (maxX, maxY) = (5D, 3D)
    val rects = Array(
      Array(
        Cube(Array(0, 0), Array(3, 1)),
        Cube(Array(0, 1), Array(3, 3), 1),
        Cube(Array(3, 0), Array(5, 1)),
        Cube(Array(3, 1), Array(5, 3), 1)
      )
    )
    val splits: Array[Array[Double]] = Array(
      0.to(xDivision).map(_ * maxX / xDivision).toArray,
      0.to(yDivision).map(_ * maxY / yDivision).toArray
    )

    // when
    val treeParser = new TreeParser(spansMid)
    implicit val mapper: Array[Cube] => Double = sumOfVolumes
    val tree = treeParser.rect2dt(mins, maxes, 0, 2, rects, splits)

    // then
    // tree values
    assert(tree.asInstanceOf[InternalSimpleNode].split.value == 2)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.value == 1)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.featureIndex == 1)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.value == 1)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[LeafSimpleNode].label == 0)
    assert(tree.asInstanceOf[InternalSimpleNode].rightChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].rightChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[LeafSimpleNode].label == 1)
  }

  test("rect2dtWithOffset") {
    val rects = Array(
      Array(
        Cube(Array(0, 0), Array(.3, 3), 1),
        Cube(Array(.3, 0), Array(1.9, 3)),
        Cube(Array(1.9, 0), Array(2.1, 3), 1),
        Cube(Array(2.1, 1.6), Array(5, 3), 1),
        Cube(Array(2.1, 0), Array(5, 1.6))
      )
    )
    val splits: Array[Array[Double]] = Array(
      Array(0, .5, 1.5, 2.5, 3.5, 4.5, 5),
      Array(0, .3, 1.3, 2.3, 1)
    )
    val (mins, maxes) = (Array(0, 0), Array(splits(0).length - 1, splits(1).length - 1))

    // when
    val treeParser = new TreeParser(spansMid)
    implicit val mapper: Array[Cube] => Double = sumOfVolumes
    val tree = treeParser.rect2dt(mins, maxes, 0, 2, rects, splits)

    // then
    // tree values
    assert(tree.asInstanceOf[InternalSimpleNode].split.value == 2.5)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.value == .5)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[LeafSimpleNode].label == 1)
    /**                                                                                     ^ dim == 1, value == 1.3                  ^ value == .3*/
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.featureIndex == 1)
    assert(tree.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].leftChild.asInstanceOf[InternalSimpleNode].split.value == 1.3)
    assert(tree.asInstanceOf[InternalSimpleNode] // dim == 0, split == 2.5
        .rightChild.asInstanceOf[InternalSimpleNode] // dim == 0, split == 3.5
        .rightChild.asInstanceOf[InternalSimpleNode] // dim == 0, split == 4.5
        .rightChild.asInstanceOf[InternalSimpleNode] // dim == 1, split == 1.3
        .rightChild.asInstanceOf[InternalSimpleNode] // dim == 1, split == 2.3
        .leftChild.asInstanceOf[LeafSimpleNode].label == 1)
  }

}
