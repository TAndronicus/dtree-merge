package jb

import java.util.stream.IntStream

import jb.conf.Config
import jb.io.FileReader.getRawInput
import jb.model.{Cube, IntegratedDecisionTreeModel, Measurements}
import jb.parser.TreeParser
import jb.prediction.Predictions.predictBaseClfs
import jb.selector.FeatureSelectors
import jb.server.SparkEmbedded
import jb.tester.FullTester.{testI, testMv, testRF, testWMv}
import jb.util.Const._
import jb.util.Util._
import jb.util.functions.WithinDeterminers._
import jb.vectorizer.FeatureVectorizers.getFeatureVectorizer
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}

class Runner(val nClassif: Int, var nFeatures: Int, val divisions: Array[Int]) {

  def calculateMvIScores(filename: String): Array[Double] = {

    //    import SparkEmbedded.ss.implicits._
    SparkEmbedded.ss.sqlContext.clearCache()

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
    val trainingSubset = if (Config.joinTrainingAndValidationSet) unionSubsets(trainingSubsets :+ cvSubset) else unionSubsets(trainingSubsets)

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

    val mvQuality = testMv(testedSubset, nClassif)
    val rfQuality = testRF(trainingSubset, testSubset, nClassif)

    val weightedMeasurementsNumber = Config.numberOfDisplacements + 1
    val weightedQuality = new Array[Measurements](Config.weightingFunctions.length * weightedMeasurementsNumber)

    for ((weightingFunction, mappingIndex) <- Config.weightingFunctions.zipWithIndex) {
      weightedQuality(mappingIndex * weightedMeasurementsNumber) = testWMv(testedSubset, nClassif, rects, weightingFunction)

      for ((division, divIndex) <- divisions.zipWithIndex) {
        var divMeasurements: List[Measurements] = Nil
        for (xStep <- 0 until Config.numberOfDisplacements; yStep <- 0 until Config.numberOfDisplacements) {
          val (minIndices, maxIndices, splits) = getDisplacementSetup(division, xStep, yStep)
          val tree = treeParser.rect2dt(minIndices, maxIndices, 0, nFeatures, rects, splits)(weightingFunction)
          val integratedModel = new IntegratedDecisionTreeModel(tree)
          val iPredictions = integratedModel.transform(testedSubset)
          divMeasurements = testI(iPredictions, testedSubset) :: divMeasurements
        }
        weightedQuality(mappingIndex * weightedMeasurementsNumber + divIndex + 1) = divMeasurements
          .reduce(_ + _) / math.pow(Config.numberOfDisplacements, 2)
      }
    }

    clearCache(subsets)

    (Array(mvQuality, rfQuality) ++ weightedQuality).flatMap(_.toArray)
  }

}
