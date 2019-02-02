package jb

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


case class FooResponse(foos: Seq[Foo])
case class Foo(id: String, typek: String, color: String)

object Playground {

      implicit val ec: ExecutionContext = ExecutionContext.global
  def main(args: Array[String]): Unit = {
//        val conf = new SparkConf().setAppName("dtree-merge").setMaster("local")
//        val ss = SparkSession.builder.config(conf).getOrCreate
//        val s1 = SparkEmbedded.ss.read.option("inferSchema", "true").format("csv").load("A/bi").rdd
//    val s1 = ss.sparkContext.textFile("A/bi")
//    println(s1.map(_.getBytes("UTF-8").length.toLong).reduce(_+_))
//        val s2 = SparkEmbedded.ss.read.option("inferSchema", "true").format("csv").load("A/a.csv")
    //    s1.unionAll(s2).except(s1.intersect(s2)).show()
    //    print("### Second ###")
    //    s1.except(s2).union(s2.except(s1)).show()


  }
}
