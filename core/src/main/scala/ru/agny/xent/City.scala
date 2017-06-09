package ru.agny.xent

import ru.agny.xent.core.utils.CityGenerator
import ru.agny.xent.core._

/**
  * City takes only one cell of a world map
  * contains visual representation of buildings/storage
  */

case class City(c: Coordinate, private val map: ShapeMap) {
  lazy val buildings = map.filter(_.building.nonEmpty).map(x => x.core)
  lazy val producers = buildings.map(x => x.building.get)

  def isEnoughSpace(s: Shape): Boolean = map.isAvailable(s)

  def update(bs: Vector[Building]): City = bs.foldLeft(this)((city, b) => update(b))

  def update(b: Building): City = copy(map = map.update(b.shape.core.copy(building = Some(b))))
}

object City {
  def empty(x: Int, y: Int): City = CityGenerator.initCity(x, y)
}
