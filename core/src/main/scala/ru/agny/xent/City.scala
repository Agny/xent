package ru.agny.xent

import ru.agny.xent.core.utils.CityGenerator
import ru.agny.xent.core.{CellsMap, LocalCell}

/**
  * City takes only one cell of a world map
  * contains visual representation of buildings/storage
  */

case class City(private val map: CellsMap[LocalCell]) {
  def find(c:LocalCell): Option[LocalCell] = map.find(c)
  def flatMap[A](c: LocalCell => Option[A]): List[A] = map.flatMap(c)
  def update(c:LocalCell):City = City(map.update(c))
}

object City {
  def empty: City = CityGenerator.initCity()
}
