package jb

import java.io.{File, PrintWriter}

import jb.server.SparkEmbedded

object ExperimentPlan {

  def main(args: Array[String]): Unit = {
    SparkEmbedded.setLogWarn()
    val nClassifs = Array(5, 7, 9)
    val nFeatures = Array(2, 3, 4, 5)
    val divisions = Array(
      Array(5),
      Array(20),
      Array(40),
      Array(80)
    )
    val pw = new PrintWriter(new File("experiments"))
    var score = 0D
    for (nC <- nClassifs; nF <- nFeatures; div <- divisions) {
      score = MultiRunner.run(nC, nF, div)
      pw.println(s"nC = $nC, nF = $nF, div = ${div(0)}")
      pw.println(score)
      pw.flush()
    }
  }

}
