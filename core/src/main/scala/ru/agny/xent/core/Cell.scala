package ru.agny.xent.core

import ru.agny.xent.core.city.Building
import ru.agny.xent.core.utils.SubTyper

trait Cell {
  val c: Coordinate
}

object Cell {
  def apply(x: Int, y: Int): Cell = EmptyCell(Coordinate(x, y))

  implicit object BuildingSubTyper extends SubTyper[Cell, Building]
}