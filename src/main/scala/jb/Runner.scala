package jb

import java.util.stream.IntStream

import jb.conf.Config
import jb.io.FileReader.getRawInput
import jb.model.{Cube, IntegratedDecisionTreeModel}
import jb.parser.TreeParser
import jb.prediction.Predictions.predictBaseClfs
import jb.selector.FeatureSelectors
import jb.server.SparkEmbedded
import jb.tester.FullTester.{testI, testMv, testWMv, testRF}
import jb.util.Const._
import jb.util.Util._
import jb.util.functions.WithinDeterminers._
import jb.vectorizer.FeatureVectorizers.getFeatureVectorizer
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}

import scala.collection.mutable.ArrayBuffer

class Runner(val nClassif: Int, var nFeatures: Int, val divisions: Array[Int]) {

  private def appendToResults(qualityMeasures: (Double, Double), results: ArrayBuffer[Double]) = {
    results += qualityMeasures._1
    results += (if(qualityMeasures._2.isNaN) 0D else qualityMeasures._2)
  }

  def calculateMvIScores(filename: String): Array[Double] = {

    //    import SparkEmbedded.ss.implicits._
    SparkEmbedded.ss.sqlContext.clearCache()
    val results = new ArrayBuffer[Double](2 /**ACC + MCC*/ * (2 /**MV + RF*/ + (1 /**wMV*/ + divisions.length * math.pow(Config.numberOfDisplacements, 2).intValue()) * Config.weightingFunctions.length))

    var input = getRawInput(filename, "csv")
    if (nFeatures > input.columns.length - 1) {
      this.nFeatures = input.columns.length - 1
      println(s"Setting nFeatures to $nFeatures")
    }
    val featureVectorizer = getFeatureVectorizer(input.columns)
    val featureSelector = FeatureSelectors.get_chi_sq_selector(nFeatures)
    val dataPrepPipeline = new Pipeline().setStages(Array(featureVectorizer, featureSelector))
    val dataPrepModel = dataPrepPipeline.fit(input)
    input = optimizeInput(input, dataPrepModel)

    val (mins, maxes) = getExtrema(input, getSelectedFeatures(dataPrepModel))

    val nSubsets = nClassif + 2
    val subsets = input.randomSplit(IntStream.range(0, nSubsets).mapToDouble(_ => 1D / nSubsets).toArray)
    recacheInput2Subsets(input, subsets)
    val (trainingSubsets, cvSubset, testSubset) = dispenseSubsets(subsets)
    val trainingSubset = if (Config.joinTrainingAndValidationSet) unionSubsets(trainingSubsets)

    def getEmptyDT = new DecisionTreeClassifier()
      .setLabelCol(LABEL)
      .setFeaturesCol(FEATURES)
      .setMaxDepth(Config.maxDepth)
      .setImpurity(Config.impurity)
    val baseModels: Array[DecisionTreeClassificationModel] = trainingSubsets.map(subset => getEmptyDT.fit(subset))

    val testedSubset = predictBaseClfs(baseModels, testSubset)

    val rootRect = Cube(mins, maxes)
    val treeParser = new TreeParser(spansMid)
    val rects: Array[Array[Cube]] = baseModels.map(model => treeParser.dt2rect(rootRect, model.rootNode))

    appendToResults(testMv(testedSubset, nClassif), results)
    appendToResults(testRF(trainingSubset, testSubset, nClassif), results)

    for (weightingFunction <- Config.weightingFunctions) {
      implicit val mapping: Array[Cube] => Double = weightingFunction
      appendToResults(testWMv(testedSubset, nClassif, rects, mapping), results)

      for (division <- divisions) {
        for (xStep <- 0 until Config.numberOfDisplacements; yStep <- 0 until Config.numberOfDisplacements) {
          val (minIndices, maxIndices, splits) = getDisplacementSetup(division, xStep, yStep)
          val tree = treeParser.rect2dt(minIndices, maxIndices, 0, nFeatures, rects, splits)
          val integratedModel = new IntegratedDecisionTreeModel(tree)
          val iPredictions = integratedModel.transform(testedSubset)
          appendToResults(testI(iPredictions, testedSubset), results)
        }
      }
    }

    clearCache(subsets)

    results.toArray
  }

}
