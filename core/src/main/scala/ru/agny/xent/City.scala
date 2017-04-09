package ru.agny.xent

import ru.agny.xent.core.Facility.State
import ru.agny.xent.core.utils.CityGenerator
import ru.agny.xent.core._

/**
  * City takes only one cell of a world map
  * contains visual representation of buildings/storage
  */

case class City(x: Int, y: Int, private val map: ShapeMap) {
  lazy val buildings = map.filter(_.building.nonEmpty).map(x => x.core)
  lazy val producers = buildings.map(x => x.building.get)

  def isEnoughSpace(s: Shape): Boolean = map.isAvailable(s)

  def build(b: Building): City = update(b, Facility.Idle)

  def update(b: Building, state: State): City = copy(map = map.update(
    b.shape.core.copy(building = Some(b.copy(state = state)))
  ))

  def update(bs: Vector[(Building, State)]): City = bs.foldLeft(this)((city, bstate) => update(bstate._1, bstate._2))
}

object City {
  def empty(x: Int, y: Int): City = CityGenerator.initCity(x, y)
}
