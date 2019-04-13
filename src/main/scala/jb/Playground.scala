package jb

import jb.server.SparkEmbedded

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.util.Random


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  def main(args: Array[String]): Unit = {
  }

}
