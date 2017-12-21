package ru.agny.xent.core.city

import ru.agny.xent.core.CellsMap

case class ShapeMap(private val cellsMap: CellsMap, private val shapes: Vector[ResultShape]) {

  def buildings: Vector[Building] = cellsMap.buildings()

  def isAvailable(s: ResultShape): Boolean = !shapes.exists(_.isIntersected(s))

  def add(b: Building, s: ResultShape): ShapeMap = ShapeMap(cellsMap.update(b), s +: shapes)

  def update(b: Building): ShapeMap = {
    buildings.find(_.id == b.id) match {
      case Some(_) => ShapeMap(cellsMap.update(b), shapes)
      case _ => this
    }
  }
}