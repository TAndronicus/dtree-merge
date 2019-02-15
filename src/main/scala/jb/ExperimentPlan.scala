package jb

import jb.server.SparkEmbedded

object ExperimentPlan {

  def main(args: Array[String]): Unit = {
    SparkEmbedded.setLogError()
    val nClassifs = Array(3, 5, 7)
    val nFeatures = 2
    val divisions = Array(60)
    for (nC <- nClassifs; div <- divisions) {
      MultiRunner.run(nC, nFeatures, div)
    }
  }

}
