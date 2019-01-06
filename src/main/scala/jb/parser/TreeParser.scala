package jb.parser

import jb.model._
import org.apache.spark.ml.tree.{ContinuousSplit, InternalNode, Node}
import jb.util.Const.EPSILON
import org.apache.spark.ml.classification.DecisionTreeClassificationModel
import org.apache.spark.mllib.tree.model.{InformationGainStats, Predict, Split}

import scala.math.floor

object TreeParser {

  def dt2rect(parent: Rect, node: Node): Array[Rect] = {
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
        val vol = parent.min.indices.map(i => parent.max(i) - parent.min(i)).product
        val mid = parent.min.indices.map(i => parent.min(i) + parent.max(i)).toArray
        Array(parent.copy(label = node.prediction, volume = vol, mid = mid))
    }
  }

  def calculateLabel(mins: Array[Double], maxes: Array[Double], rects: Array[Array[Rect]]): Double = {
    rects.map(
      geometricalRepresentation => geometricalRepresentation.filter(_.isWithin(mins, maxes))
    )
  }

  def rect2dt(mins: Array[Double], maxes: Array[Double], elSize: Array[Double], dim: Int, maxDim: Int, rects: Array[Array[Rect]]): SimpleNode = {
    var diff = maxes(dim) - mins(dim)
    if (diff > elSize(dim) + EPSILON) {
      val mid = mins(dim) + floor(diff / (2 * elSize(dim))) * elSize(dim)
      val (newMins, newMaxes) = (mins.clone(), maxes.clone())
      newMins(dim) = diff
      newMaxes(dim) = diff
      InternalSimpleNode(rect2dt(mins, newMaxes, elSize, dim, maxDim, rects), rect2dt(newMins, maxes, elSize, dim, maxDim, rects),
        new SimpleSplit(dim, diff))
    } else if (dim < maxDim) {
      val newDim = dim + 1
      diff = maxes(newDim) - mins(newDim)
      val (newMins, newMaxes) = (mins.clone(), maxes.clone())
      newMins(dim) = diff
      newMaxes(dim) = diff
      InternalSimpleNode(rect2dt(mins, newMaxes, elSize, newDim, maxDim, rects), rect2dt(newMins, maxes, elSize, newDim, maxDim, rects),
        new SimpleSplit(newDim, diff))
    } else {
      LeafSimpleNode(calculateLabel(mins, maxes, rects))
    }
  }

}