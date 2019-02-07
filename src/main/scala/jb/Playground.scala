package jb

import jb.server.SparkEmbedded
import org.apache.spark.sql.expressions.Window

import scala.concurrent.ExecutionContext
import org.apache.spark.sql.functions._


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {
        import SparkEmbedded.ss.implicits._
    val df = SparkEmbedded.ss.createDataFrame(Seq(
      (1, 2),
      (2, 4),
      (3, 6),
      (4, 5),
      (5, 6),
      (6, 8)
    )).toDF("id", "c")
    val columns = df.columns
    var df1 = df.select(columns.map(col):_*)
    var df2 = df.select(columns.map(col):_*)
    columns.foreach(name => df1 = df1.withColumnRenamed(name, name + "1"))
    columns.foreach(name => df2 = df2.withColumnRenamed(name, name + "2"))
    df.join(df1, $"id" === $"id1" - 1).join(df2, $"id" === $"id2" + 1).withColumn("windowF", $"c" / 2 + $"c1" / 4 + $"c2" / 4).show()

    df.createTempView("tab")
    SparkEmbedded.ss
      .sql("select 0.5 * t1.c + 0.25 * t2.c + 0.25 * t3.c result " +
        "from tab t1 join tab t2 on t1.id = t2.id - 1 join tab t3 on t1.id = t3.id + 1").show()


    println("heh")




  }
}
