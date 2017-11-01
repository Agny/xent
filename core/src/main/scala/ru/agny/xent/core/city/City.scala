package ru.agny.xent.core.city

import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core._
import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.{CityGenerator, ErrorCode}

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

  def place(b: Building, s: ResultShape): Either[ErrorCode.Value, City] = {
    if (map.isAvailable(s)) Right(copy(map = map.add(b, s)))
    else Left(ErrorCode.BUILDING_CANT_BE_PLACED)
  }

  def update(b: Building, s: Storage = storage): City = copy(map = map.update(b), storage = s)

  def addResources(v: Vector[Item]): City = copy(storage = storage.add(v)._1)

  private def updateMap(bs: Vector[Building]): ShapeMap = bs.foldLeft(map)((m, b) => m.update(b))
}

object City {
  def empty(x: Int, y: Int, s: Storage = Storage.empty): City = CityGenerator.initCity(x, y, s)
}
