package ru.agny.xent.core.city

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.city.Shape.FourShape
import ru.agny.xent.core.inventory._

class CityTest extends FlatSpec with Matchers with EitherValues {

  import TestHelper._

  val worker = defaultSoul(1)
  val bName = "Test"
  val shape = FourShape.name

  "City" should "place building in the map" in {
    val prod = Producible(1, "Test prod", ProductionSchema(500, Cost(Vector.empty), Set.empty))
    val place = Coordinate(2, 1)
    val b = Building(place, bName, Vector(prod), 500)
    val city = City.empty(0, 0)
    val res = city.place(b, Shape.values(shape).form(place)).right.value
    city should not be res
  }

  it should "update facilities state upon producing" in {
    val place = Coordinate(2, 1)
    val prodId = 2
    val prodTime = 1000
    val progress = 1200
    val prod = Producible(prodId, "Test prod", ProductionSchema(prodTime, Cost(Vector.empty), Set.empty))
    val building = {
      val (b, _) = Building(place, bName, Vector(prod), 500).finish.run(worker)
      b.addToQueue(ItemStack(2, prodId))(Storage.empty).right.value._2
    }
    val res = City.empty(0, 0).place(building, Shape.values(shape).form(Coordinate(2, 1)))
    val city = res.right.value.produce(progress)
    city.producers.find(_.name == bName).get.queue.progress should be(progress - prodTime)
  }
}
