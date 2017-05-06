package ru.agny.xent.core

/**
  * All coordinates are axial
  */
case class Coordinate(x: Int, y: Int) extends Cell

/**
  * Helper class for distance calculation
  * x + y + z = 0
  */
case class Cube(x: Int, z: Int) {
  val y = -x - z
}

case class Path(cells: Vector[Coordinate]) {
  def probe(idx: Int): Coordinate = cells(idx)
}

object Coordinate {
  def distance(start: Cube, end: Cube): Int = {
    math.max(math.max(math.abs(start.x - end.x), math.abs(start.y - end.y)), math.abs(start.z - end.z))
  }

  def path(start: Coordinate, end: Coordinate): Path = path_rec(start, end, Vector(end))

  private def path_rec(start: Coordinate, end: Coordinate, acc: Vector[Coordinate]): Path = {
    val (sx, sy) = (start.x, start.y)
    val (ex, ey) = (end.x, end.y)
    val xdiff = math.abs(ex - sx)
    val ydiff = math.abs(ey - sy)
    (xdiff, ydiff) match {
      case endOfPath if xdiff == 0 && ydiff == 0 => Path(acc)
      case stepByX if xdiff >= ydiff =>
        val step = if (ex > sx) -1 else 1
        val c = Coordinate(ex + step, ey)
        path_rec(start, c, c +: acc)
      case stepByY if xdiff < ydiff =>
        val step = if (ey > sy) -1 else 1
        val c = Coordinate(ex, ey + step)
        path_rec(start, c, c +: acc)
    }
  }

  implicit class CubeI(c: Coordinate) {
    def to(other: Coordinate): Int = {
      val from = Cube(c.x, c.y)
      val to = Cube(other.x, other.y)
      distance(from, to)
    }
  }
}