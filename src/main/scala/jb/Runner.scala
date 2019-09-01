package jb

import java.util.stream.IntStream

import jb.conf.Config
import jb.io.FileReader.getRawInput
import jb.model.{Cube, IntegratedDecisionTreeModel}
import jb.parser.TreeParser
import jb.prediction.Predictions.predictBaseClfs
import jb.selector.FeatureSelectors
import jb.server.SparkEmbedded
import jb.tester.FullTester.{testI, testMv, testRF}
import jb.util.Const._
import jb.util.Util._
import jb.util.functions.WeightAggregators._
import jb.util.functions.WithinDeterminers._
import jb.vectorizer.FeatureVectorizers.getFeatureVectorizer
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{ DecisionTreeClassificationModel, DecisionTreeClassifier }

class Runner(val nClassif: Int, var nFeatures: Int, val divisions: Int) {

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
    val trainingSubset = unionSubsets(trainingSubsets)

    val dt = new DecisionTreeClassifier()
      .setLabelCol(LABEL)
      .setFeaturesCol(FEATURES)
      .setMaxDepth(Config.maxDepth)
      .setImpurity(Config.impurity)
    val baseModels: Array[DecisionTreeClassificationModel] = trainingSubsets.map(subset => dt.fit(subset))

    val testedSubset = predictBaseClfs(baseModels, testSubset)
    val mvQualityMeasure = testMv(testedSubset, nClassif)
    val rfQualityMeasure = testRF(trainingSubset, testSubset, nClassif)

    val rootRect = Cube(mins, maxes)
    val treeParser = new TreeParser(sumOfVolumes, spansMid)
    val rects = baseModels.map(model => treeParser.dt2rect(rootRect, model.rootNode))
    val elSize = getElCubeSize(mins, maxes, divisions)
    val tree = treeParser.rect2dt(mins, maxes, elSize, 0, nFeatures, rects)
    val integratedModel = new IntegratedDecisionTreeModel(tree)
    val iPredictions = integratedModel.transform(testedSubset)
    val iQualityMeasure = testI(iPredictions, testedSubset)

    clearCache(subsets)

    Array(mvQualityMeasure._1, if(mvQualityMeasure._2.isNaN) 0D else mvQualityMeasure._2,
      rfQualityMeasure._1, if(rfQualityMeasure._2.isNaN) 0D else rfQualityMeasure._2,
      iQualityMeasure._1, if(iQualityMeasure._2.isNaN) 0D else iQualityMeasure._2)
  }

}
