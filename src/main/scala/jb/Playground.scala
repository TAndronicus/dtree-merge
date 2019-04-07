package jb

import jb.server.SparkEmbedded

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.util.Random


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    isList(List(1, 2, 3))
  }

  def sth[A, B, C]: (A => B) => C = ???

  def parseDouble(value: Any): Double = {
    value match {
      case int: Int =>
        int.toDouble
      case double: Double =>
        double
    }
  }

  def isList(a: Any): Unit = {
    a match {
      case a: List[AnyVal] => println("Heh")
      case _ => println("Jednak nie")
    }
  }

}
