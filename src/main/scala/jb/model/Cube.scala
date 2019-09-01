package jb.model

case class Cube(var min: Array[Double], var max: Array[Double], var label: Double = 0D) {

  def isWithin(rowsWithin: (Array[Double], Array[Double]) => Boolean): Boolean = {
    rowsWithin(min, max)
  }

  def contains(point: Array[Double]): Boolean = {
    point.indices.forall(index => min(index) <= point(index) && max(index) >= point(index))
  }

  override def toString: String = "Min: " + min.map(item => item.toString).reduce((s1, s2) => s1 + ", " + s2) + ", Max: " +
    max.map(_.toString).reduce((s1, s2) => s1 + ", " + s2) + ", Label: " + label.toString + ", Mid: " +
    mid.map(_.toString).reduce((s1, s2) => s1 + ", " + s2) + ", Volume: " + volume.toString

  def mid: Array[Double] = {
    min.indices.map(i => (max(i) + min(i)) / 2D).toArray
  }

  def volume: Double = {
    min.indices.map(i => max(i) - min(i)).product
  }

}
