package ru.agny.xent.core.city

import ru.agny.xent.core.{CellsMap, LocalCell}

case class ShapeMap(private val cellsMap: CellsMap[LocalCell], private val shapes: Vector[ResultShape]) {
  def buildings: Vector[Building] = withBuildings().map(_.building.get)

  def isAvailable(s: ResultShape): Boolean = !shapes.exists(_.isIntersected(s))

  def add(b: Building, s: ResultShape): ShapeMap = {
    val lc = LocalCell(s.core.x, s.core.y, Some(b))
    ShapeMap(cellsMap.update(lc), s +: shapes)
  }

  def update(b: Building): ShapeMap = {
    withBuildings().find(_.building.get.id == b.id) match {
      case Some(v) if containsSameBuilding(v, b) => ShapeMap(cellsMap.update(v.copy(building = Some(b))), shapes)
      case _ => this
    }
  }

  private def containsSameBuilding(c: LocalCell, b: Building): Boolean = {
    val r = for {
      cb <- c.building
    } yield cb.id == b.id
    r.getOrElse(false)
  }

  private def withBuildings(): Vector[LocalCell] = cellsMap.filter(_.building.nonEmpty)
}