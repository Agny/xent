package ru.agny.xent.realm

import scala.annotation.tailrec

case class Path(from: Coordinate, to: Coordinate) {
  private val cells = path_rec(from, to, Seq(to))

  def probe(idx: Int): Coordinate = idx match {
    case inBound if cells.isDefinedAt(idx) => cells(idx)
    case negative if idx < 0 => cells.head
    case maxed if idx >= cells.length => cells.last
  }

  // tiles to walk through, start point is removed as it is reached already
  def tiles = cells.length - 1

  /**
    * We are walking from end to start and building sequence of steps by prepending,
    * so result will be ordered from start to end
    */
  @tailrec private def path_rec(start: Coordinate, end: Coordinate, acc: Seq[Coordinate]): Seq[Coordinate] = {
    val (sx, sy) = (start.x, start.y)
    val (ex, ey) = (end.x, end.y)
    val xdiff = math.abs(ex - sx)
    val ydiff = math.abs(ey - sy)
    (xdiff, ydiff) match {
      case endOfPath if xdiff == 0 && ydiff == 0 => acc
      case stepByX if xdiff >= ydiff =>
        val step = if (ex > sx) -1 else 1
        val c = Coordinate(ex + step, ey)
        path_rec(start, c, c +: acc)
      case stepByY =>
        val step = if (ey > sy) -1 else 1
        val c = Coordinate(ex, ey + step)
        path_rec(start, c, c +: acc)
    }
  }
}
