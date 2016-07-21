package ru.agny.xent.core.utils

import ru.agny.xent.City
import ru.agny.xent.core._

object CityGenerator {

  private val initBuilding = "Coppice"

  private def resourceGen(layerLvl: Int): List[ProducibleTemplate] = TemplateLoader.loadProducibles(layerLvl.toString)

  private def buildingGen(layerLvl: Int): List[BuildingTemplate] = TemplateLoader.loadBuildings(layerLvl.toString)

  def initCity(): City = {
    val res = resourceGen(1)
    val building = buildingGen(1)
    val mbBuilding = building.find(b => b.name == initBuilding).map(bt => {
      val resources = res.filter(pt => bt.resources.contains(pt.name)).map(pt => Producible(pt.name, pt.cost, pt.yieldTime, Set.empty))
      Building(1, bt.name, resources, bt.cost)
    })

    val map = CellsMap(0 to 2 map (x => 0 to 2 map (y => {
      if (x == 1 && y == 1) LocalCell(x, y, mbBuilding)
      else LocalCell(x, y)
    }) toVector) toVector)
    City(map)
  }

}
