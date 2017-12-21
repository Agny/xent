package ru.agny.xent.core

import ru.agny.xent.core.city.Building

case class CellsMap(private val cells: Vector[Vector[Cell]]) {
  val length = cells.length

  def find(c: Coordinate): Option[Cell] = {
    (c.x, c.y) match {
      case (x, y) if (x >= 0 && x < length) && (y >= 0 && y < length) => Some(cells(x)(y))
      case _ => None
    }
  }

  def buildings(): Vector[Building] = cells.flatten.collect { case c: Building => c }

  def filter(c: Cell => Boolean): Vector[Cell] = {
    cells.flatMap(x => rec_filter(c)(x, Vector.empty))
  }

  private def rec_filter(c: Cell => Boolean)(elems: Vector[Cell], acc: Vector[Cell]): Vector[Cell] = {
    elems match {
      case h +: t => if (c(h)) rec_filter(c)(t, h +: acc) else rec_filter(c)(t, acc)
      case Vector() => acc
    }
  }

  def update(c: Cell): CellsMap = {
    find(c.c) match {
      case Some(v) =>
        val yLayer = cells(v.c.x).updated(v.c.y, c)
        CellsMap(cells.updated(v.c.x, yLayer))
      case None => this
    }
  }

  //TODO extract visible range parameters
  private val xScreen = 7
  private val yScreen = 5

  def view(x: Int, y: Int) = {
    val (fx, tx) = getRange(x, xScreen)
    val (fy, ty) = getRange(y, yScreen)
    val xInRange = isInRange(fx, tx) _
    val yInRange = isInRange(fy, ty) _
    filter(c => xInRange(c.c.x) && yInRange(c.c.y))
  }

  private def isInRange(from: Int, to: Int)(i: Int) =
    i >= (from % length + length) % length &&
      i <= (to % length + length) % length

  private def getRange(i: Int, range: Int) = {
    (i - range / 2, i + range / 2)
  }

}

