package ru.agny.xent.core

import org.scalatest.{FlatSpec, Matchers}
import ru.agny.xent.core.city.{Building, Shape, ShapeMap}
import ru.agny.xent.core.city.Shape.FourShape
import ru.agny.xent.core.inventory.{Cost, Producible, ProductionSchema}
import ru.agny.xent.core.utils.{CityGenerator, TimeUnit}

class ShapeMapTest extends FlatSpec with Matchers {

  val bName = "Test"
  val shape = FourShape.name

  "ShapeMap" should "add building and shape" in {
    val p = Producible(1, "Test res", ProductionSchema(TimeUnit.minute + 1000, Cost(Vector.empty), Set.empty))
    val building = Building(bName, Vector(p), 0)
    val cm = CityGenerator.generateCityMap(4)
    val m = ShapeMap(cm, Vector.empty)
    val shapeToAdd = Shape.values(shape).form(Coordinate(2, 1))

    m.isAvailable(shapeToAdd) should be(true)
    val res = m.add(building, shapeToAdd)
    res.buildings.contains(building) should be(true)
    res.isAvailable(shapeToAdd) should be(false)
  }

  it should "update existing building" in {
    val p = Producible(1, "Test res", ProductionSchema(TimeUnit.minute + 1000, Cost(Vector.empty), Set.empty))
    val building = Building(bName, Vector(p), 0).build
    val cm = CityGenerator.generateCityMap(4)
    val m = ShapeMap(cm, Vector.empty)
    val shapeMap = m.add(building, Shape.values(shape).form(Coordinate(2, 1)))
    val updated = building.finish
    val res = shapeMap.update(updated)

    res.buildings.contains(building) should be(false)
    res.buildings.contains(updated) should be(true)
  }
}
