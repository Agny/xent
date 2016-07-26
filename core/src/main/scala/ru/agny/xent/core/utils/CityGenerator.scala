package ru.agny.xent.core.utils

import ru.agny.xent.City
import ru.agny.xent.core._

object CityGenerator {

  private val initBuilding = "Coppice"

  private def buildingGen(layerLvl: Int): List[BuildingTemplate] = TemplateLoader.loadBuildings(layerLvl.toString)

  def initCity(): City = {
    val building = buildingGen(1)
    val mbBuilding = building.find(b => b.name == initBuilding).map(bt => Building(1, bt.name, bt.resources, bt.cost))

    val map = CellsMap(0 to 2 map (x => 0 to 2 map (y => {
      if (x == 1 && y == 1) LocalCell(x, y, mbBuilding)
      else LocalCell(x, y)
    }) toVector) toVector)
    City(map)
  }

}