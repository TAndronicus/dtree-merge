package jb.parser

import jb.model._
import org.apache.spark.ml.tree.{ContinuousSplit, InternalNode, Node}

import scala.math.floor

class TreeParser(rowWithinStrategy: (Array[Double], Array[Double]) => (Array[Double], Array[Double]) => Boolean) {

  // TODO: optimize cpu
  def dt2rect(parent: Cube, node: Node): Array[Cube] = {
    node match {
      case interNode: InternalNode =>
        val newMax = parent.max.clone()
        newMax(interNode.split.featureIndex) = interNode.split.asInstanceOf[ContinuousSplit].threshold
        val newMin = parent.min.clone()
        newMin(interNode.split.featureIndex) = interNode.split.asInstanceOf[ContinuousSplit].threshold

        val leftChild = parent.copy(max = newMax)
        val rightChild = parent.copy(min = newMin)

        dt2rect(leftChild, node.asInstanceOf[InternalNode].leftChild) ++ dt2rect(rightChild, node.asInstanceOf[InternalNode].rightChild)
      case _ =>
        Array(parent.copy(label = node.prediction))
    }
  }

  // TODO: optimize cpu
  def calculateLabel(weightAggregator: Array[Cube] => Double, mins: Array[Double], maxes: Array[Double], rects: Array[Array[Cube]]): Double = {
    rects.map(
      geometricalRepresentation => geometricalRepresentation.filter(_.isWithin(rowWithinStrategy(mins, maxes))) // filtering ones that span the cube
        .groupBy(_.label)
        .mapValues(weightAggregator) // sum weights (volumes)
        .reduce((a1, a2) => if (a1._2 > a2._2) a1 else a2)._1 // choosing label with the greatest value
    ).groupBy(identity).reduce((l1, l2) => if (l1._2.length > l2._2.length) l1 else l2)._1 // chosing label with the greatest count
  }

  def rect2dt(mins: Array[Int], maxes: Array[Int], dim: Int, maxDim: Int, rects: Array[Array[Cube]], splits: Array[Array[Double]])
             (implicit weightAggregator: Array[Cube] => Double): SimpleNode = {
    if (maxes(dim) - mins(dim) > 1) {
      stepDown(mins, maxes, dim, maxDim, rects, splits)
    } else if (dim < maxDim - 1) {
      stepDown(mins, maxes, dim + 1, maxDim, rects, splits)
    } else {
      LeafSimpleNode(calculateLabel(weightAggregator, getBoundaries(mins, splits), getBoundaries(maxes, splits), rects))
    }
  }

  private def stepDown(mins: Array[Int], maxes: Array[Int], dim: Int, maxDim: Int, rects: Array[Array[Cube]], splits: Array[Array[Double]])
                      (implicit weightAggregator: Array[Cube] => Double): InternalSimpleNode = {
    val mid: Int = getMid(mins, maxes, dim)
    val (newMins, newMaxes) = (mins.clone(), maxes.clone())
    newMins(dim) = mid
    newMaxes(dim) = mid
    InternalSimpleNode(rect2dt(mins, newMaxes, dim, maxDim, rects, splits), rect2dt(newMins, maxes, dim, maxDim, rects, splits), new SimpleSplit(dim, splits(dim)(mid)))
  }

  private def getBoundaries(splitIndices: Array[Int], splits: Array[Array[Double]]): Array[Double] = {
    splitIndices.indices.map(index => splits(index)(splitIndices(index))).toArray
  }

  private def getMid(mins: Array[Int], maxes: Array[Int], dim: Int): Int = {
    floor((mins(dim) + maxes(dim)) / 2).intValue()
  }

}
