package ru.agny.xent.core

import ru.agny.xent.City
import ru.agny.xent.UserType.UserId

sealed trait Cell {
  val x, y: Int
}
case class WorldCell(x: Int, y: Int, resource: Option[Extractable] = None, city: Option[City] = None, owner: Option[UserId] = None) extends Cell
case class LocalCell(x: Int, y: Int, building: Option[Building] = None) extends Cell

case class CellsMap[T <: Cell](val cells: Vector[Vector[T]]) {

  def find(c: T): Option[T] = {
    (c.x, c.y) match {
      case (x, y) if (x >= 0 && x < cells.length) && (y >= 0 && y < cells(x).length) => Some(cells(x)(y))
      case _ => None
    }
  }

  def update(c: T): CellsMap[T] = {
    find(c) match {
      case Some(v) =>
        val yLayer = cells(v.x).updated(v.y, c)
        CellsMap(cells.updated(v.x, yLayer))
      case None => this
    }
  }

}

object CellsMap {
  def localDefault(): CellsMap[LocalCell] = CellsMap(0 to 2 map (x => 0 to 2 map (y => LocalCell(x, y)) toVector) toVector)
}
