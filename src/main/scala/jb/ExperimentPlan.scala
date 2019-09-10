package jb

import jb.conf.Config
import jb.server.SparkEmbedded

object ExperimentPlan {

  def main(args: Array[String]): Unit = {
    SparkEmbedded.setLogError()
    for (nC <- Config.nClassifs) {
      MultiRunner.run(nC, Config.nFeatures, Config.divisions)
    }
  }

}
