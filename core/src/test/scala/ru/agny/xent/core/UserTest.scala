package ru.agny.xent.core

import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper
import ru.agny.xent.action.{DoNothing, PlaceBuilding, ResourceClaim}
import ru.agny.xent.battle.{Military, Movement, Waiting}
import ru.agny.xent.core.city.Shape.FourShape
import ru.agny.xent.core.city._
import ru.agny.xent.core.inventory._
import ru.agny.xent.core.utils.{BuildingTemplate, OutpostTemplate}

class UserTest extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  import TestHelper._
  import ru.agny.xent.core.inventory.Item.implicits._

  val shape = FourShape.name
  val waitingCoordinate = new Waiting(Coordinate(0, 0), 0)
  val woodId = 1
  val soulOne = defaultSoul(1)
  val soulTwo = defaultSoul(2)
  val user = defaultUser()

  override protected def beforeAll(): Unit = {
    ShapeProvider.add(BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, ""))
  }

  override protected def afterAll(): Unit = {
    ShapeProvider.delete("Test")
  }

  "User" should "create troop from the souls" in {
    val soul1 = (soulOne, waitingCoordinate)
    val soul2 = (soulTwo, waitingCoordinate)
    val souls = Workers(Vector(soul1, soul2))
    val userWithSouls = user.copy(souls = souls)
    val (soulless, troop) = userWithSouls.createTroop(3, Vector(1, 2)).right.value
    soulless.souls should be(Workers.empty)
    troop.activeUnits should be(Vector(soul1._1, soul2._1))
  }

  it should "not take occupied souls to the troop" in {
    val soul1 = (soulOne, waitingCoordinate)
    val soul2 = (soulTwo, Movement(Coordinate(0, 0), Coordinate(1, 2)))
    val souls = Workers(Vector(soul1, soul2))
    val userWithSouls = user.copy(souls = souls)
    val (userWithSoul, troop) = userWithSouls.createTroop(3, Vector(1, 2)).right.value
    userWithSoul.souls should be(Workers(Vector(soul2)))
    troop.activeUnits should be(Vector(soul1._1))
  }

  it should "add production to facility queue" in {
    val toProduce = Producible(1, "meh", ProductionSchema(1000, Cost(Vector.empty), Set.empty))
    val building = Building(Coordinate(2, 2), "Test", Vector(toProduce), 1000).finish
    val facilityId = building.id
    val city = City.empty(0, 0).place(building, Shape.values(shape).form(building.c))
    val user = User(1, "Vasya", city.right.value)
    val res = user.addProduction(facilityId, ItemStack(4, 1))
    val queue = res.right.value.city.producers.find(_.id == facilityId).get.queue
    queue.content should be(Vector((toProduce, 4)))
  }

  "PlaceBuildingAction" should "spend resources" in {
    val bt = BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, "")
    val layer = Layer("", 1, Vector.empty, Military.empty, CellsMap(Vector.empty), Vector(bt))
    val action = PlaceBuilding("Test", layer, Coordinate(2, 1))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val updated = userAndStorage.work(action)
    val expected = Vector(ItemStack(3, woodId))
    updated.right.value.city.storage.resources should be(expected)
  }

  it should "add building to the city" in {
    val buildingConstructionTime = 10
    val bt = BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), buildingConstructionTime, shape, "")
    val layer = Layer("", 1, Vector.empty, Military.empty, CellsMap(Vector.empty), Vector(bt))
    val action = PlaceBuilding(bt.name, layer, Coordinate(2, 1))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val updated = userAndStorage.work(action).right.value

    Thread.sleep(buildingConstructionTime)
    val userWithBuilding = updated.work(DoNothing).right.value
    val mbBuilding = userWithBuilding.city.producers.find(c => c.name == bt.name)
    mbBuilding.isEmpty shouldBe false
  }

  "ResourceClaimAction" should "spend resources" in {
    val bt = OutpostTemplate("Test", "Test res", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, "")
    val place = Coordinate(1, 2)
    val resourceToClaim = ResourceCell(place, Extractable(1, "Test res", 10, 111, Set.empty))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val layer = Layer("", 1, Vector(userAndStorage), Military.empty, CellsMap(Vector(Vector(), Vector(Cell(1, 0), Cell(1, 1), resourceToClaim), Vector())), Vector(bt))
    val action = ResourceClaim("Test", user.id, resourceToClaim.c)
    val updated = layer.tick(action)
    val expected = Vector(ItemStack(3, woodId))
    updated.right.value.users.head.city.storage.resources should be(expected)
  }

}
