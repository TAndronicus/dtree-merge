package jb

import java.io.{File, PrintWriter}

import jb.util.Const.FILENAME_PREFIX
import jb.util.ResultCatcher

object MultiRunner {

  val resultCatcher = new ResultCatcher(.3, 10, 100)

  def run(nClassif: Int, nFeatures: Int, divisions: Array[Int]): Double = {
    val reps = 1
    val filenames = Array("bi", "bu", "c", "d", "h", "i", "m", "p", "se", "so", "sp", "t", "wd", "wi")

    val runner = new Runner(nClassif, nFeatures, divisions)
    val finalScores = runForFiles(reps, runner)(filenames)

    writeScores(finalScores)
  }

  private def runForFiles(reps: Int, runner: Runner)(filenames: Array[String]) = {
    while (resultCatcher.canConsume && !resultCatcher.isFilled) {
      val scores = new Array[Array[Double]](filenames.length)
      for (index <- filenames.indices) {
        println(s"File: ${filenames(index)}")
        scores(index) = runner.calculateMvIScores(FILENAME_PREFIX + filenames(index))
      }
    }
    resultCatcher.aggregate
  }

  def writeScores(finalScores: Array[Array[Double]]): Unit = {
    val pw = new PrintWriter(new File("result"))
    finalScores.foreach(scores => pw.println(scores.map(_.toString).reduce((s1, s2) => s1 + "," + s2)))
    pw.flush()
    pw.close()
  }

}
