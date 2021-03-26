package ru.agny.xent.realm

import scala.annotation.tailrec

case class Path(from: Hexagon, to: Hexagon) {
  private val cells = path_rec(from, to, Seq(to))
  private var cursor = from

  def placeCursor(idx: Int): Hexagon = idx match {
    case inBound if cells.isDefinedAt(idx) =>
      cursor = cells(idx)
      cursor
    case negative if idx < 0 =>
      cursor = cells.head
      cursor
    case maxed if idx >= cells.length =>
      cursor = cells.last
      cursor
  }

  def next(): Hexagon = placeCursor(cells.indexOf(cursor) + 1)

  /**
   * We are walking from end to start and building sequence of steps by prepending,
   * so result will be ordered from start to end
   */
  @tailrec private def path_rec(start: Hexagon, end: Hexagon, acc: Seq[Hexagon]): Seq[Hexagon] = {
    val (sx, sy) = (start.x, start.y)
    val (ex, ey) = (end.x, end.y)
    val xdiff = math.abs(ex - sx)
    val ydiff = math.abs(ey - sy)
    (xdiff, ydiff) match {
      case endOfPath if xdiff == 0 && ydiff == 0 => acc
      case stepByX if xdiff >= ydiff =>
        val step = if (ex > sx) -1 else 1
        val c = Hexagon(ex + step, ey)
        path_rec(start, c, c +: acc)
      case stepByY =>
        val step = if (ey > sy) -1 else 1
        val c = Hexagon(ex, ey + step)
        path_rec(start, c, c +: acc)
    }
  }
}
