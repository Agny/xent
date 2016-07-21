package ru.agny.xent

import ru.agny.xent.core.utils.CityGenerator
import ru.agny.xent.core.{CellsMap, LocalCell}

/**
  * City takes only one cell of a world map
  * contains visual representation of buildings/storage
  */

case class City(map: CellsMap[LocalCell])

object City {
  def empty: City = CityGenerator.initCity()
}
