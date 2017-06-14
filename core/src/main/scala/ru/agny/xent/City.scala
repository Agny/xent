package ru.agny.xent

import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.utils.{SubTyper, CityGenerator}
import ru.agny.xent.core._

/**
  * City takes only one cell of a world map
  * contains visual representation of buildings/storage
  */

case class City(c: Coordinate, private val map: ShapeMap, storage: Storage) {

  import FacilitySubTyper.implicits._

  lazy val buildings = map.filter(_.building.nonEmpty).map(x => x.core)
  lazy val producers = buildings.map(x => x.building.get)

  def produce(period: ProgressTime, outposts: Vector[Outpost]): (City, Vector[Outpost]) = {
    val (s, p) = storage.tick(period, producers ++ outposts)
    val (buildings, outs) = SubTyper.partition[Building, Outpost, Facility](p)
    (City(c, updateMap(buildings), s), outs)
  }

  def update(b: Building, s: Storage = storage): City = copy(map = updateMap(b), storage = s)

  def isEnoughSpace(s: Shape): Boolean = map.isAvailable(s)

  private def updateMap(bs: Vector[Building]): ShapeMap = bs.foldLeft(map)((m, b) => updateMap(b))

  private def updateMap(b: Building): ShapeMap = map.update(b.shape.core.copy(building = Some(b)))
}

object City {
  def empty(x: Int, y: Int, s: Storage = Storage.empty): City = CityGenerator.initCity(x, y, s)
}
