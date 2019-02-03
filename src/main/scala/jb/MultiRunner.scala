package jb

import java.io.{File, PrintWriter}

import jb.server.SparkEmbedded
import jb.util.Const.FILENAME_PREFIX

object MultiRunner {

  def main(args: Array[String]): Unit = {
    SparkEmbedded.setLogWarn()
    val reps = 1
    val filenames = Array("bi", "bu", "c", "d", "h", "i", "m", "p", "se", "so", "sp", "t", "wd", "wi")
    val nClassif = 3
    val nFeatures = 4
    val divisions = Array(20, 40, 60)

    val runner = new Runner(nClassif, nFeatures, divisions)
    val finalScores = runForFiles(reps, runner)(filenames)

    writeScores(finalScores)
    println(finalScores.map(sc => if (sc.last > sc.head) 1 else 0).sum.toDouble / finalScores.length)
  }

  private def runForFiles(reps: Int, runner: Runner)(filenames: Array[String]) = {
    val finalScores = new Array[Array[Double]](filenames.length)
    for (index <- filenames.indices) {
      println(s"File: ${filenames(index)}")
      finalScores(index) = runReps(reps, runner, filenames(index))
    }
    finalScores
  }

  private def runReps(reps: Int, runner: Runner, filename: String) = {
    val meanScores = new Array[Double](runner.divisions.length + 1)
    for (_ <- 0.until(reps)) {
      val scores = runner.calculateMvIScores(FILENAME_PREFIX + filename)
      scores.indices.foreach(i => meanScores(i) += scores(i))
    }
    meanScores.map(_ / reps)
  }

  def writeScores(finalScores: Array[Array[Double]]): Unit = {
    val pw = new PrintWriter(new File("result"))
    finalScores.foreach(scores => pw.println(scores.map(_.toString).reduce((s1, s2) => s1 + "," + s2)))
    pw.flush()
    pw.close()
  }

}
