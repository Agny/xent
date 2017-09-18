package ru.agny.xent.core

import ru.agny.xent.core.city.Building
import ru.agny.xent.core.utils.SubTyper

trait Cell {
  val c: Coordinate
}

object Cell {
  def apply(x: Int, y: Int): Cell = CellInner(Coordinate(x, y))

  private case class CellInner(c: Coordinate) extends Cell

  implicit object BuildingSubTyper extends SubTyper[Cell, Building] {
    override def asSub(a: Cell) = a match {
      case b: Building => Some(b)
      case _ => None
    }
  }
}