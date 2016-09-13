package ru.agny.xent

import ru.agny.xent.core.utils.CityGenerator
import ru.agny.xent.core.{Cell, CellsMap, LocalCell}

/**
  * City takes only one cell of a world map
  * contains visual representation of buildings/storage
  */

case class City(x: Int, y: Int, private val map: CellsMap[LocalCell]) {
  def find(c: Cell): Option[LocalCell] = map.find(c)

  def filter(c: LocalCell => Boolean): Seq[LocalCell] = map.filter(c)

  def flatMap[A](c: LocalCell => Option[A]): Seq[A] = map.flatMap(c)

  def update(c: LocalCell): City = City(x, y, map.update(c))
}

object City {
  def empty(x: Int, y: Int): City = CityGenerator.initCity(x, y)
}
