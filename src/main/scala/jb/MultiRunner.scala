package jb

import java.io.{File, PrintWriter}

import jb.util.Const.FILENAME_PREFIX

object MultiRunner {

  def run(nClassif: Int, nFeatures: Int, divisions: Array[Int]): Double = {
    val reps = 1
    val filenames = Array("bi", "bu", "c", "d", "h", "i", "m", "p", "se", "so", "sp", "t", "wd", "wi")

    val runner = new Runner(nClassif, nFeatures, divisions)
    val finalScores = runForFiles(reps, runner)(filenames)

    //    writeScores(finalScores)
    finalScores.map(sc => if (sc.last > sc.head) 1 else 0).sum.toDouble / finalScores.length
  }

  private def runForFiles(reps: Int, runner: Runner)(filenames: Array[String]) = {
    val scores = new Array[Array[Double]](filenames.length)
    for (index <- filenames.indices) {
      println(s"File: ${filenames(index)}")
      scores(index) = runner.calculateMvIScores(FILENAME_PREFIX + filenames(index))
    }
    Array(Array(.1, .2))
  }

  def writeScores(finalScores: Array[Array[Double]]): Unit = {
    val pw = new PrintWriter(new File("result"))
    finalScores.foreach(scores => pw.println(scores.map(_.toString).reduce((s1, s2) => s1 + "," + s2)))
    pw.flush()
    pw.close()
  }

}
