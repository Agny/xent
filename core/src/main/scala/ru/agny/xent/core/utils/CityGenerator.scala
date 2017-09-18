package ru.agny.xent.core.utils

import ru.agny.xent.core._
import ru.agny.xent.core.city._

object CityGenerator {

  private val initBuilding = "Coppice"

  private def buildingGen(layerLvl: Int): Vector[BuildingTemplate] = TemplateLoader.loadBuildings(layerLvl.toString)

  def initCity(x: Int, y: Int, s: Storage): City = {
    val cellsMap = generateCityMap(3)
    val (b, shape) = createDefaultBuilding()
    val map = ShapeMap(cellsMap, Vector.empty).add(b, shape)
    City(Coordinate(x, y), map, s)
  }

  def generateCityMap(size: Int): CellsMap = {
    CellsMap((0 to size).toVector.map(x => (0 to size).toVector.map(Cell(x, _))))
  }

  private def createDefaultBuilding(): (Building, ResultShape) = {
    val building = buildingGen(1)
    val c = Coordinate(0, 1)
    building.find(b => b.name == initBuilding).map(bt =>
      (Building(c, bt.name, bt.producibles, bt.buildTime).finish, ShapeProvider.get(bt.name).form(c))).get
  }

}
