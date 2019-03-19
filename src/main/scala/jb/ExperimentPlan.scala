package jb

import jb.server.SparkEmbedded

object ExperimentPlan {

  def main(args: Array[String]): Unit = {
    SparkEmbedded.setLogError()
    val nClassifs = Array(7)
    val nFeatures = 2
    val divisions = Array(20, 40, 60)
    for (nC <- nClassifs; div <- divisions) {
      MultiRunner.run(nC, nFeatures, div)
    }
  }

}
