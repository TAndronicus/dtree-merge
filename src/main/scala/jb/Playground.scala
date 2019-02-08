package jb

import jb.server.SparkEmbedded
import org.apache.spark.sql.functions._

import scala.concurrent.ExecutionContext


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val df = SparkEmbedded.ss.createDataFrame(Seq(
      (1, 2),
      (2, 4),
      (3, 6),
      (4, 5),
      (5, 6),
      (6, 8)
    )).toDF("id", "c")
    val (a, b, c) = (.25, .25, .5)

    df.createTempView("tab")

    SparkEmbedded.ss.sql("create table result(c decimal)")
    SparkEmbedded.ss.sql("select t1.c from ")
    Array("E", "F", "G", "H").map(col).map(first)

    println("heh")


  }
}
