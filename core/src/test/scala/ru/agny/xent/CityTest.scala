package ru.agny.xent

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.core.Shape.FourShape
import ru.agny.xent.core._
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.unit.{Level, Spirit, Soul}

class CityTest extends FlatSpec with Matchers with EitherValues {

  val worker = Soul(1, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
  val bName = "Test"
  val shape = FourShape.name

  "City" should "place building in the map" in {
    val prod = Producible(1, "Test prod", ProductionSchema(500, Cost(Vector.empty), Set.empty))
    val b = Building(bName, Vector(prod), 500)
    val city = City.empty(0, 0)
    val res = city.place(b, Shape.values(shape).form(Coordinate(2, 1))).right.value
    city should not be res
  }

  it should "update facilities state upon producing" in {
    val prodId = 2
    val extrTime = 1100
    val prodTime = 1000
    val progress = 1200
    val extr = Extractable(1, "Test res", 100, extrTime, Set.empty)
    val prod = Producible(prodId, "Test prod", ProductionSchema(prodTime, Cost(Vector.empty), Set.empty))
    val (o, _) = Outpost("outpost", extr, Vector.empty, 500).finish.run(worker)
    val building = {
      val (b, _) = Building(bName, Vector(prod), 500).finish.run(worker)
      b.addToQueue(ItemStack(2, prodId))(Storage.empty).right.value._2
    }
    val res = City.empty(0, 0).place(building, Shape.values(shape).form(Coordinate(2, 1)))
    val (city, Vector(outpost)) = res.right.value.produce(progress, Vector(o))
    outpost.queue.progress should be(progress - extrTime)
    city.producers.find(_.name == bName).get.queue.progress should be(progress - prodTime)
  }
}
