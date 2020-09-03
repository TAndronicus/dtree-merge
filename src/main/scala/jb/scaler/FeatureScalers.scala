package jb.scaler

import jb.util.Const
import org.apache.spark.ml.PipelineStage
import org.apache.spark.ml.feature.MinMaxScaler

object FeatureScalers {

  def minMaxScaler: PipelineStage = new MinMaxScaler().setInputCol(Const.NON_SCALED_FEATURES).setOutputCol(Const.FEATURES)

}
