package ru.agny.xent

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.battle.{Waiting, Military, Movement}
import ru.agny.xent.battle.core.LevelBar
import ru.agny.xent.battle.unit.inventory.Equipment
import ru.agny.xent.battle.unit.{SpiritBar, Soul}
import ru.agny.xent.core.utils.{OutpostTemplate, BuildingTemplate}
import ru.agny.xent.core._

class UserTest extends FlatSpec with Matchers with EitherValues {

  import Item.implicits._

  val shape = FourShape(LocalCell(0, 0))
  val waitingCoordinate = new Waiting(Coordinate(0, 0), 0)
  val woodId = 1
  val buildingId = 1

  "User" should "spend resources" in {
    val user = User(1, "test", City.empty(0, 0, Storage(Vector(ItemStack(10, woodId)))), Lands.empty, ProductionQueue.empty, Workers.empty, 0)
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, "")
    val updated = user.spend(bt.cost)
    val expected = Storage(Vector(ItemStack(3, woodId)))
    updated.right.value.city.storage should be(expected)
  }

  it should "not spend any resources if there is not enough" in {
    val user = User(1, "test", City.empty(0, 0, Storage(Vector(ItemStack(5, woodId)))), Lands.empty, ProductionQueue.empty, Workers.empty, 0)
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, "")
    val updated = user.spend(bt.cost)
    updated.isLeft should be(true)
  }

  it should "create troop from the souls" in {
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

  "PlaceBuildingAction" should "spend resources" in {
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, "")
    val layer = Layer("", 1, Vector.empty, Military.empty, CellsMap(Vector.empty), Vector(bt))
    val user = User(1, "test", City.empty(0, 0))
    val action = PlaceBuilding("Test", layer, LocalCell(2, 1))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val updated = userAndStorage.work(action)
    val expected = Vector(ItemStack(3, woodId))
    updated.right.value.city.storage.resources should be(expected)
  }

  "PlaceBuildingAction" should "add building to city" in {
    val buildingConstructionTime = 10
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), buildingConstructionTime, shape, "")
    val layer = Layer("", 1, Vector.empty, Military.empty, CellsMap(Vector.empty), Vector(bt))
    val user = User(1, "test", City.empty(0, 0))
    val bCell = LocalCell(2, 1)
    val action = PlaceBuilding("Test", layer, bCell)
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val updated = userAndStorage.work(action).right.value

    Thread.sleep(buildingConstructionTime)
    val userWithBuilding = updated.work(DoNothing).right.value
    val expected = bt.name
    val updatedCell = userWithBuilding.city.buildings.find(c => c.x == bCell.x && c.y == bCell.y)
    updatedCell.isEmpty shouldBe false
    updatedCell.get.building.isEmpty shouldBe false
    updatedCell.get.building.get.name should be(expected)
  }

  "ResourceClaimAction" should "spend resources" in {
    val bt = OutpostTemplate(buildingId, "Test", "Test res", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, "")
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
    val ot = OutpostTemplate(buildingId, "Out Test", "Test res", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, "")
    val bt = BuildingTemplate(buildingId, "Build Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, "")
    val user = User(1, "test", City.empty(0, 0))
    val resourceToClaim = WorldCell(1, 2, Some(Extractable(1, "Test res", 10, 111, Set.empty)))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(15, woodId)))))
    val layer = Layer("", 1, Vector(userAndStorage), Military.empty, CellsMap(Vector(Vector(), Vector(WorldCell(1, 0), WorldCell(1, 1), resourceToClaim), Vector())), Vector(ot, bt))
    val resourceClaim = ResourceClaim("Out Test", 1, WorldCell(1, 2))
    val placeBuilding = PlaceBuilding("Build Test", layer, LocalCell(2, 1))
    val updated = layer.tick(resourceClaim)
    val lastUpdated = updated.right.value.tick(placeBuilding, userAndStorage.id)
    val expected = ItemStack(1, woodId)
    lastUpdated.right.value.users.head.city.storage.get(woodId) should be(Some(expected))
  }

}
