package jb

import jb.server.SparkEmbedded

import scala.concurrent.ExecutionContext


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {

    val a = SparkEmbedded.ss.read.format("csv").option("inferSchema", "true").load("A/bi")
    val b = SparkEmbedded.ss.read.format("csv").option("inferSchema", "true").load("A/so")
    //    val a = SparkEmbedded.ss.sql("select * from a where exists(select * from b where a._c41 = b._c60)")
    //    import org.apache.spark.sql.functions.col
    //    val c = a.select(b.columns.map(col):_*)

    val ar = Array(1 -> Array(2, 4), 2 -> Array(3, 2), 3 -> Array(4, 5, 3), 4 -> Array(5, 1, 2), 5 -> Array(6))
    import SparkEmbedded.ss.implicits._
    //    val ar = List(1, 2, 3, 4, 5).toDF.toJavaRDD
    ar.foreach(row => Array(parseDouble(row._1)).toSeq.toDF().show())
    //    ar.foreach(row => println(row))
    //    ar.foreach(el => el)
  }

  def parseDouble(value: Any): Double = {
    value match {
      case int: Int =>
        int.toDouble
      case double: Double =>
        double
    }
  }
}
