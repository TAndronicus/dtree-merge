package jb.conf

object Config {

  /** Models */
  val maxDepth: Int = 3
  val impurity = "gini"

  /** Result catcher */
  val treshold: Double = .2
  val batch: Int = 10
  val minIter: Int = 50
  val maxIter: Int = Int.MaxValue

}
