package jb

import jb.conf.Config
import jb.model.{Cube, Measurements}
import jb.util.Const.FILENAME_PREFIX
import jb.util.functions.WeightAggregators
import jb.util.result.{GeneralCatcher, ResultCatcher}

object MultiRunner {

  def run(nClassif: Int, nFeatures: Int, divisions: Array[Int]): Unit = {
    val filenames = Array(
      "aa",
      "ap",
      "ba",
      "bi",
      "bu",
      "c",
      "d",
      "ec",
      "h",
      "i",
      "ir",
      "m",
      "ma",
      "p",
      "ph",
      "pi",
      "ri",
      "sb",
      "se",
      "t",
      "te",
      "th",
      "ti",
      "wd",
      "wi",
      "wr",
      "ww",
      "ye"
    )
    // for 4 dimensions

    val runner = new Runner(nClassif, nFeatures, divisions)
    val resultCatcher = runForFiles(runner)(filenames)

    resultCatcher.header = composeHeader(Config.weightingFunctions, divisions)

    /** numberOfClassifiers_numberOfDimensions_[numberOfDivisions]_numberOfDisplacements */
    resultCatcher.writeScores(Array(nClassif.toString, nFeatures.toString, divisions.mkString("[", "_", "]"), Config.numberOfDisplacements.toString))
  }

  private def runForFiles(runner: Runner)(filenames: Array[String]): ResultCatcher = {
    val resultCatcher = getResultCatcher
    while (resultCatcher.canConsume && !resultCatcher.isFull) {
      try {
        val scores = new Array[Array[Double]](filenames.length)
        for (index <- filenames.indices) {
          scores(index) = runner.calculateMvIScores(FILENAME_PREFIX + filenames(index))
        }
        resultCatcher.consume(scores)
      } catch {
        case e: Throwable => println("Caught: " + e.getMessage) // Print message and retry
      }
    }
    resultCatcher

  }

  private def getResultCatcher: ResultCatcher = {
    new GeneralCatcher(Config.treshold, Config.batch, Config.minIter, Config.maxIter)
  }

  def composeHeader(weightingFunctions: Array[Array[Cube] => Double], divisions: Array[Int]): Array[String] = {
    val header = for {
      ref <- Array("MV", "RF")
      meas <- Measurements.meas
    } yield s"${ref}(${meas})"
    header ++ (for {
      method <- weightingFunctions.map(WeightAggregators.names(_))
      division <- "wMV" +: divisions.map("Psi_" + _)
      measurement <- Measurements.meas
    } yield s"$division^$method($measurement)")
  }

}
