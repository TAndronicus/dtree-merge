package jb

import jb.model.Rect
import jb.server.SparkEmbedded
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object Playground {

  def main(args: Array[String]): Unit = {
//    val conf = new SparkConf().setAppName("dtree-merge").setMaster("local")
//    val ss = SparkSession.builder.config(conf).getOrCreate
//    val s1 = SparkEmbedded.ss.read.option("inferSchema", "true").format("csv").load("A/b.csv")
//    val s2 = SparkEmbedded.ss.read.option("inferSchema", "true").format("csv").load("A/a.csv")
//    s1.unionAll(s2).except(s1.intersect(s2)).show()
//    print("### Second ###")
//    s1.except(s2).union(s2.except(s1)).show()
    val a = Array(0, 1, 2)
    print(a.length / 2D)
    val b = (v: Array[Rect]) => v.map(_.volume).sum
  }

}
