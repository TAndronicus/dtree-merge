package jb

import jb.util.Const.FILENAME_PREFIX
import jb.util.result.EagerResultCatcher

object MultiRunner {

  val resultCatcher = new EagerResultCatcher(.3, 10, 100)

  def run(nClassif: Int, nFeatures: Int, divisions: Int): Unit = {
    val filenames = Array("bi", "bu", "c", "d", "h", "i", "m", "p", "se", "so", "sp", "t", "wd", "wi")

    val runner = new Runner(nClassif, nFeatures, divisions)
    val finalScores = runForFiles(runner)(filenames)

    resultCatcher.writeScores(finalScores)
  }

  private def runForFiles(runner: Runner)(filenames: Array[String]) = {
    while (resultCatcher.canConsume && !resultCatcher.isFilled) {
      val scores = new Array[Array[Double]](filenames.length)
      for (index <- filenames.indices) {
        println(s"File: ${filenames(index)}")
        scores(index) = runner.calculateMvIScores(FILENAME_PREFIX + filenames(index))
      }
      resultCatcher.consume(scores)
    }
    resultCatcher.aggregate
  }

}
