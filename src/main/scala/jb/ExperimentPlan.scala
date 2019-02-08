package jb

import jb.server.SparkEmbedded

object ExperimentPlan {

  def main(args: Array[String]): Unit = {
    SparkEmbedded.setLogWarn()
    val nClassifs = 5
    val nFeatures = 2
    val divisions = 20
    MultiRunner.run(nClassifs, nFeatures, divisions)
  }

}
