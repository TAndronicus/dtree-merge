package jb

import jb.server.SparkEmbedded

import scala.concurrent.ExecutionContext


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {

    SparkEmbedded.ss.read.format("csv").option("inferSchema", "true").load("A/bi").createTempView("a")
    SparkEmbedded.ss.read.format("csv").option("inferSchema", "true").load("A/so").createTempView("b")
    val a = SparkEmbedded.ss.sql("select * from a where exists(select * from b where a._c41 = b._c60)")
    a.show()

  }
}
