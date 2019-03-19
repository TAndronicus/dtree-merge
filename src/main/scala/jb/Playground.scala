package jb

import jb.server.SparkEmbedded

import scala.concurrent.ExecutionContext
import scala.util.Random


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {

    val a = (for (_ <- 0 until 5) yield Seq.fill(3)(Random.nextInt(2))).map(x => (x(0), x(1), x(2)))
    import SparkEmbedded.ss.implicits._
    a.toDF.show
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
