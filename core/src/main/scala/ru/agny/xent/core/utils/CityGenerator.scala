package ru.agny.xent.core.utils

import ru.agny.xent.City
import ru.agny.xent.core._

object CityGenerator {

  private val initBuilding = "Coppice"

  private def buildingGen(layerLvl: Int): Vector[BuildingTemplate] = TemplateLoader.loadBuildings(layerLvl.toString)

  def initCity(x: Int, y: Int, s: Storage): City = {
    val building = buildingGen(1)
    val mbBuilding = building.find(b => b.name == initBuilding).map(bt => Building(bt.id, bt.name, bt.producibles, bt.buildTime, bt.shape))

    val map = ShapeMap(CellsMap((0 to 3).toVector.map(x => (0 to 3).toVector.map(y => {
      if (x == 0 && y == 1) LocalCell(x, y, mbBuilding)
      else LocalCell(x, y)
    }))))
    City(Coordinate(x, y), map, s)
  }

}
