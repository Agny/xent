package ru.agny.xent.core.city

import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core._
import ru.agny.xent.core.utils.{CityGenerator, SubTyper}
import ru.agny.xent.messages.Response

/**
  * City takes only one cell of a world map
  * contains visual representation of buildings/storage
  */

case class City(c: Coordinate, private val map: ShapeMap, storage: Storage) extends Cell {

  lazy val producers = map.buildings.filter(_.isFunctioning)

  def produce(period: ProgressTime): City = {
    val (s, buildings) = storage.tick(period, producers)
    City(c, updateMap(buildings), s)
  }

  def place(b: Building, s: ResultShape): Either[Response, City] = {
    if (map.isAvailable(s)) Right(copy(map = map.add(b, s)))
    else Left(Response(s"Not enough space to place $s"))
  }

  def update(b: Building, s: Storage = storage): City = copy(map = map.update(b), storage = s)

  private def updateMap(bs: Vector[Building]): ShapeMap = bs.foldLeft(map)((m, b) => m.update(b))
}

object City {
  def empty(x: Int, y: Int, s: Storage = Storage.empty): City = CityGenerator.initCity(x, y, s)
}
