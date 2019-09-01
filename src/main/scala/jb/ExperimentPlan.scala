package jb

import jb.server.SparkEmbedded

object ExperimentPlan {

  def main(args: Array[String]): Unit = {
    SparkEmbedded.setLogError()
    val nFeatures = 2
    val nClassifs = Array(7, 9)
//    val nClassifs = Array(3)
    val divisions = Array(20)
//    val divisions = Array(20)
    for (nC <- nClassifs) {
      MultiRunner.run(nC, nFeatures, divisions)
    }
  }

}
