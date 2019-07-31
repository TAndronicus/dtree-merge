package jb.util

import jb.util.Const._
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.feature.ChiSqSelectorModel
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, Dataset, Row}

object Util {

  // TODO: optimize cpu
  def getExtrema(input: DataFrame, selectedFeatures: Array[Int]): (Array[Double], Array[Double]) = {
    var paramMap = List.newBuilder[(String, String)]
    for (item <- selectedFeatures.sorted; fun <- Array("min", "max")) {
      paramMap += (COL_PREFIX + item -> fun)
    }
    val extrema = input.agg(paramMap.result().head, paramMap.result().drop(1): _*).head.toSeq.toIndexedSeq
    val mins = extrema.sliding(1, 2).flatten.map(value => parseDouble(value)).toArray
    val maxs = extrema.drop(1).sliding(1, 2).flatten.map(value => parseDouble(value)).toArray
    (mins, maxs)
  }

  def parseDouble(value: Any): Double = {
    value match {
      case int: Int =>
        int.toDouble
      case double: Double =>
        double
    }
  }

  def optimizeInput(input: DataFrame, dataPrepModel: PipelineModel): DataFrame = {
    dataPrepModel.transform(input).select(
      Util.getSelectedFeatures(dataPrepModel).map(
        item => col(COL_PREFIX + item)
      ).+:(col(FEATURES)).+:(col(LABEL)): _*
    ).persist
  }

  def getSelectedFeatures(dataPrepModel: PipelineModel): Array[Int] = {
    dataPrepModel.stages(1).asInstanceOf[ChiSqSelectorModel].selectedFeatures
  }

  def clearCache(subsets: Array[Dataset[Row]]) = {
    subsets.foreach(_.unpersist)
  }

  def recacheInput2Subsets(input: DataFrame, subsets: Array[DataFrame]): Unit = {
    input.unpersist
    subsets.foreach(_.cache)
  }

  def dispenseSubsets(subsets: Array[DataFrame]): (Array[DataFrame], DataFrame, DataFrame) = {
    val trainingSubsets = subsets.take(subsets.length - 2)
    val cvSubset = subsets(subsets.length - 2)
    val testSubset = subsets.last
    (trainingSubsets, cvSubset, testSubset)
  }

  def unionSubsets(subsets: Array[DataFrame]): DataFrame ={
    subsets.reduce((s1, s2) => s1.union(s2))
  }

  def getElCubeSize(mins: Array[Double], maxes: Array[Double], division: Int): Array[Double] = {
    mins.indices.map(i => (maxes(i) - mins(i)) / division).toArray
  }

  def testAggregate(container: Array[Array[Array[Double]]]): Array[Array[Double]] = {
    val result = new Array[Array[Double]](container(0).length)
    for (midIndex <- container(0).indices) {
      val partialResult = new Array[Double](container(0)(0).length)
      for (innerIndex <- container(0)(0).indices) {
        partialResult(innerIndex) = container.map(score => score(midIndex)(innerIndex)).sum / container.length
      }
      result(midIndex) = partialResult
    }
    result
  }

}
