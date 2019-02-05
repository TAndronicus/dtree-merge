package jb

import jb.server.SparkEmbedded
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.concurrent.ExecutionContext


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("dtree-merge").setMaster("local")
    val ss = SparkSession.builder.config(conf).getOrCreate
    //    val s1 = ss.sparkContext.textFile("A/bi")
    //        val s2 = SparkEmbedded.ss.read.option("inferSchema", "true").format("csv").load("A/a.csv")
    //    s1.unionAll(s2).except(s1.intersect(s2)).show()
    //    s1.except(s2).union(s2.except(s1)).show()

    val df = SparkEmbedded.ss.createDataFrame(Seq(
      (1, 2, 5),
      (2, 4, 7),
      (3, 6, 9)
    )).toDF("label", "min", "max")

    print("heh")



  }
}
