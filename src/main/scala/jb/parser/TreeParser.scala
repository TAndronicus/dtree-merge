package jb.parser

import jb.model._
import jb.util.Const.EPSILON
import org.apache.spark.ml.tree.{ContinuousSplit, InternalNode, Node}

import scala.math.floor

class TreeParser(val weightAggregator: Array[Cube] => Double, rowWithin: (Array[Double], Array[Double]) => (Array[Double], Array[Double]) => Boolean) {

  // TODO: optimize cpu
  def dt2rect(parent: Cube, node: Node): Array[Cube] = {
    node match {
      case _: InternalNode =>
        val interNode = node.asInstanceOf[InternalNode]

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
  def calculateLabel(mins: Array[Double], maxes: Array[Double], rects: Array[Array[Cube]]): Double = {
    rects.map(
      geometricalRepresentation => geometricalRepresentation.filter(_.isWithin(rowWithin(mins, maxes))) // filtering ones that span the cube
        .groupBy(_.label)
        .mapValues(weightAggregator) // sum weights (volumes)
        .reduce((a1, a2) => if (a1._2 > a2._2) a1 else a2)._1 // choosing label with the greatest value
    ).groupBy(identity).reduce((l1, l2) => if (l1._2.length > l2._2.length) l1 else l2)._1 // chosing label with the greatest count
  }

  def rect2dt(mins: Array[Double], maxes: Array[Double], elSize: Array[Double], dim: Int, maxDim: Int, rects: Array[Array[Cube]]): SimpleNode = {
    var diff = maxes(dim) - mins(dim)
    if (diff > elSize(dim) + EPSILON) {
      val mid = mins(dim) + floor((diff + EPSILON) / (2 * elSize(dim))) * elSize(dim)
      val (newMins, newMaxes) = (mins.clone(), maxes.clone())
      newMins(dim) = mid
      newMaxes(dim) = mid
      InternalSimpleNode(rect2dt(mins, newMaxes, elSize, dim, maxDim, rects), rect2dt(newMins, maxes, elSize, dim, maxDim, rects), new SimpleSplit(dim, mid))
    } else if (dim < maxDim - 1) {
      val newDim = dim + 1
      diff = maxes(newDim) - mins(newDim)
      val mid = mins(newDim) + floor((diff + EPSILON) / (2 * elSize(newDim))) * elSize(newDim)
      val (newMins, newMaxes) = (mins.clone(), maxes.clone())
      newMins(newDim) = mid
      newMaxes(newDim) = mid
      InternalSimpleNode(rect2dt(mins, newMaxes, elSize, newDim, maxDim, rects), rect2dt(newMins, maxes, elSize, newDim, maxDim, rects), new SimpleSplit(newDim, mid))
    } else {
      LeafSimpleNode(calculateLabel(mins, maxes, rects))
    }
  }

}
