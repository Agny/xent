package ru.agny.xent

import ru.agny.xent.core.utils.CityGenerator
import ru.agny.xent.core._

/**
  * City takes only one cell of a world map
  * contains visual representation of buildings/storage
  */

case class City(x: Int, y: Int, private val map: ShapeMap) {
  def isEnoughSpace(s: Shape): Boolean = map.isAvailable(s)

  def buildings(): Seq[Shape] = map.filter(_.building.nonEmpty)

  def build(c: LocalCell, b: Building): City = ???
}

object City {
  def empty(x: Int, y: Int): City = CityGenerator.initCity(x, y)
}
