package ru.agny.xent.core

/**
  * All coordinates are axial
  */
case class Coordinate(x: Int, y: Int) extends Cell {

  import Coordinate._

  def distance(to: Coordinate): Int =
    math.max(math.max(math.abs(this.x - to.x), math.abs(this.y - to.y)), math.abs(this.z - to.z))

  def path(to: Coordinate) = Path(this, to)
}

object Coordinate {
  private case class Cube(x: Int, y: Int) {
    val z = -x - y
  }

  private implicit class CubeI(c: Coordinate) {
    def z: Int = Cube(c.x, c.y).z
  }
}