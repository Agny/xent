package ru.agny.xent

import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, FlatSpec}
import ru.agny.xent.battle.{Waiting, Military, Movement}
import ru.agny.xent.battle.core.LevelBar
import ru.agny.xent.battle.unit.inventory.Equipment
import ru.agny.xent.battle.unit.{SpiritBar, Soul}
import ru.agny.xent.core.utils.{OutpostTemplate, BuildingTemplate}
import ru.agny.xent.core._

class UserTest extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  import Item.implicits._

  val shape = FourShape(Coordinate(0, 0))
  val waitingCoordinate = new Waiting(Coordinate(0, 0), 0)
  val woodId = 1

  override protected def beforeAll(): Unit = {
    ShapeProvider.add(BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, ""))
  }

  override protected def afterAll(): Unit = {
    ShapeProvider.delete("Test")
  }

  "User" should "create troop from the souls" in {
    val soul1 = (Soul(1, LevelBar(0, 0, 0), SpiritBar(1, 1, 0), Equipment.empty, 0, Vector.empty), waitingCoordinate)
    val soul2 = (Soul(2, LevelBar(0, 0, 0), SpiritBar(1, 1, 0), Equipment.empty, 0, Vector.empty), waitingCoordinate)
    val souls = Workers(Vector(soul1, soul2))
    val user = User(1, "Vasya", City.empty(0, 0), Lands.empty, ProductionQueue.empty, souls, 0)
    val (soulless, troop) = user.createTroop(3, Vector(1, 2))
    soulless.souls should be(Workers.empty)
    troop.get.activeUnits should be(Vector(soul1._1, soul2._1))
  }

  it should "not take occupied souls to the troop" in {
    val soul1 = (Soul(1, LevelBar(0, 0, 0), SpiritBar(1, 1, 0), Equipment.empty, 0, Vector.empty), waitingCoordinate)
    val soul2 = (Soul(2, LevelBar(0, 0, 0), SpiritBar(1, 1, 0), Equipment.empty, 0, Vector.empty), Movement(Coordinate(0, 0), Coordinate(1, 2), 0))
    val souls = Workers(Vector(soul1, soul2))
    val user = User(1, "Vasya", City.empty(0, 0), Lands.empty, ProductionQueue.empty, souls, 0)
    val (userWithSoul, troop) = user.createTroop(3, Vector(1, 2))
    userWithSoul.souls should be(Workers(Vector(soul2)))
    troop.get.activeUnits should be(Vector(soul1._1))
  }

  it should "add production to facility queue" in {
    val toProduce = Producible(1, "meh", ProductionSchema(1000, Cost(Vector.empty), Set.empty))
    val building = Building("Test", Vector(toProduce), 1000).finish
    val facilityId = building.id
    val city = City.empty(0, 0).place(building, shape.form(Coordinate(2, 2)))
    val user = User(1, "Vasya", city.right.value, Lands.empty, ProductionQueue.empty, Workers.empty, 0)
    val res = user.addProduction(facilityId, ItemStack(4, 1))
    val queue = res.right.value.city.producers.find(_.id == facilityId).get.queue
    queue.content should be(Vector((toProduce, 4)))
  }

  "PlaceBuildingAction" should "spend resources" in {
    val bt = BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, "")
    val layer = Layer("", 1, Vector.empty, Military.empty, CellsMap(Vector.empty), Vector(bt))
    val user = User(1, "test", City.empty(0, 0))
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
    val user = User(1, "test", City.empty(0, 0))
    val bCell = Coordinate(2, 1)
    val action = PlaceBuilding(bt.name, layer, bCell)
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val updated = userAndStorage.work(action).right.value

    Thread.sleep(buildingConstructionTime)
    val userWithBuilding = updated.work(DoNothing).right.value
    val mbBuilding = userWithBuilding.city.producers.find(c => c.name == bt.name)
    mbBuilding.isEmpty shouldBe false
  }

  "ResourceClaimAction" should "spend resources" in {
    val bt = OutpostTemplate("Test", "Test res", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, "")
    val user = User(1, "test", City.empty(0, 0))
    val resourceToClaim = WorldCell(1, 2, Some(Extractable(1, "Test res", 10, 111, Set.empty)))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val layer = Layer("", 1, Vector(userAndStorage), Military.empty, CellsMap(Vector(Vector(), Vector(WorldCell(1, 0), WorldCell(1, 1), resourceToClaim), Vector())), Vector(bt))
    val action = ResourceClaim("Test", 1, WorldCell(1, 2))
    val updated = layer.tick(action)
    val expected = Vector(ItemStack(3, woodId))
    updated.right.value.users.head.city.storage.resources should be(expected)
  }

  "Sequential actions" should "spend resources" in {
    val ot = OutpostTemplate("Out Test", "Test res", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, "")
    val bt = BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, "")
    val user = User(1, "test", City.empty(0, 0))
    val resourceToClaim = WorldCell(1, 2, Some(Extractable(1, "Test res", 10, 111, Set.empty)))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(15, woodId)))))
    val layer = Layer("", 1, Vector(userAndStorage), Military.empty, CellsMap(Vector(Vector(), Vector(WorldCell(1, 0), WorldCell(1, 1), resourceToClaim), Vector())), Vector(ot, bt))
    val resourceClaim = ResourceClaim("Out Test", 1, WorldCell(1, 2))
    val placeBuilding = PlaceBuilding("Test", layer, Coordinate(2, 1))
    val updated = layer.tick(resourceClaim)
    val lastUpdated = updated.right.value.tick(placeBuilding, userAndStorage.id)
    val expected = ItemStack(1, woodId)
    lastUpdated.right.value.users.head.city.storage.get(woodId) should be(Some(expected))
  }

}
