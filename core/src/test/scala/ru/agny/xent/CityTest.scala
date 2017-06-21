package ru.agny.xent

import org.scalatest.{EitherValues, BeforeAndAfterAll, Matchers, FlatSpec}
import ru.agny.xent.battle.core.LevelBar
import ru.agny.xent.battle.unit.inventory.Equipment
import ru.agny.xent.battle.unit.{SpiritBar, Soul}
import ru.agny.xent.core.utils.{BuildingTemplate, TimeUnit}
import ru.agny.xent.core._

class CityTest extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  val worker = Soul(1, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
  val bName = "Test"

  override protected def beforeAll(): Unit = {
    ShapeProvider.add(BuildingTemplate(bName, Vector.empty, Vector.empty, Cost(Vector.empty), 0, FourShape(Coordinate(0, 0)), ""))
  }

  override protected def afterAll(): Unit = {
    ShapeProvider.delete(bName)
  }

  "City" should "place building in the map" in {
    val prod = Producible(1, "Test prod", ProductionSchema(500, Cost(Vector.empty), Set.empty))
    val b = Building(bName, Vector(prod), 500)
    val city = City.empty(0, 0)
    val res = city.place(b, ShapeProvider.get("Test").form(Coordinate(2, 1))).right.value
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
    val res = City.empty(0, 0).place(building, ShapeProvider.get("Test").form(Coordinate(2, 1)))
    val (city, Vector(outpost)) = res.right.value.produce(progress, Vector(o))
    outpost.queue.progress should be(progress - extrTime)
    city.producers.find(_.name == bName).get.queue.progress should be(progress - prodTime)
  }
}
