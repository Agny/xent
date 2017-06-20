package ru.agny.xent.core.utils

import ru.agny.xent.City
import ru.agny.xent.core._

object CityGenerator {

  private val initBuilding = "Coppice"

  private def buildingGen(layerLvl: Int): Vector[BuildingTemplate] = TemplateLoader.loadBuildings(layerLvl.toString)

  def initCity(x: Int, y: Int, s: Storage): City = {
    val cellsMap = generateCityMap(3)
    val (b, shape) = createDefaultBuilding()
    val map = ShapeMap(cellsMap, Vector.empty).add(b, shape)
    City(Coordinate(x, y), map, s)
  }

  def generateCityMap(size: Int): CellsMap[LocalCell] = {
    CellsMap((0 to size).toVector.map(x => (0 to size).toVector.map(LocalCell(x, _))))
  }

  private def createDefaultBuilding(): (Building, ResultShape) = {
    val building = buildingGen(1)
    building.find(b => b.name == initBuilding).map(bt => (Building(bt.name, bt.producibles, bt.buildTime).finish, bt.shape.form(Coordinate(0, 1)))).get
  }

}
