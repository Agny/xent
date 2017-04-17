package ru.agny.xent

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.core.utils.{IdHolder, OutpostTemplate, BuildingTemplate}
import ru.agny.xent.core._

class UserTest extends FlatSpec with Matchers with EitherValues {

  import Item.implicits._

  val shape = FourShape(LocalCell(0,0))
  val woodId = 1
  val buildingId = 1

  "User" should "spend resources" in {
    val user = User(1, "test", City.empty(0, 0), Lands.empty, Storage(Vector(ResourceUnit(10, woodId))), ProductionQueue.empty, 0)
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector(ResourceUnit(7, woodId)), 0, shape, "")
    val updated = user.spend(bt)
    val expected = Storage(Vector(ResourceUnit(3, woodId)))
    updated.right.value.storage should be(expected)
  }

  it should "not spend any resources if there is not enough" in {
    val user = User(1, "test", City.empty(0, 0), Lands.empty, Storage(Vector(ResourceUnit(5, woodId))), ProductionQueue.empty, 0)
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector(ResourceUnit(7, woodId)), 0, shape, "")
    val updated = user.spend(bt)
    updated.isLeft should be(true)
  }

  "Newly created user" should "spend resources" in {
    val user = User(1, "test", City.empty(0, 0))
    val userAndStorage = user.copy(storage = Storage(Vector(ResourceUnit(10, woodId))))
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector(ResourceUnit(7, woodId)), 0, shape, "")
    val updated = userAndStorage.spend(bt)
    val expected = Vector(ResourceUnit(3, woodId))
    updated.right.value.storage.resources should be(expected)
  }

  "PlaceBuildingAction" should "spend resources" in {
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector(ResourceUnit(7, woodId)), 0, shape, "")
    val layer = Layer("", 1, Vector.empty, CellsMap(Vector.empty), Vector(bt))
    val user = User(1, "test", City.empty(0, 0))
    val action = PlaceBuilding("Test", layer, LocalCell(2, 1))
    val userAndStorage = user.copy(storage = Storage(Vector(ResourceUnit(10, woodId))))
    val updated = userAndStorage.work(action)
    val expected = Vector(ResourceUnit(3, woodId))
    updated.right.value.storage.resources should be(expected)
  }

  "PlaceBuildingAction" should "add building to city" in {
    val buildingConstructionTime = 10
    val bt = BuildingTemplate(buildingId, "Test", Vector.empty, Vector(ResourceUnit(7, woodId)), buildingConstructionTime, shape, "")
    val layer = Layer("", 1, Vector.empty, CellsMap(Vector.empty), Vector(bt))
    val user = User(1, "test", City.empty(0, 0))
    val bCell = LocalCell(2, 1)
    val action = PlaceBuilding("Test", layer, bCell)
    val userAndStorage = user.copy(storage = Storage(Vector(ResourceUnit(10, woodId))))
    val updated = userAndStorage.work(action).right.value

    Thread.sleep(buildingConstructionTime)
    val userWithBuilding = updated.work(DoNothing).right.value
    val expected = bt.name
    val updatedCell = userWithBuilding.city.buildings.find(c => c.x == bCell.x && c.y == bCell.y)
    updatedCell.isEmpty shouldBe false
    updatedCell.get.building.isEmpty shouldBe false
    updatedCell.get.building.get.name should be (expected)
  }

  "ResourceClaimAction" should "spend resources" in {
    val bt = OutpostTemplate(buildingId, "Test", "Test res", Vector.empty, Vector(ResourceUnit(7, woodId)), 0, "")
    val user = User(1, "test", City.empty(0, 0))
    val resourceToClaim = WorldCell(1, 2, Some(Extractable(1, "Test res", 10, 111, Set.empty)))
    val userAndStorage = user.copy(storage = Storage(Vector(ResourceUnit(10, woodId))))
    val layer = Layer("", 1, Vector(userAndStorage), CellsMap(Vector(Vector(), Vector(WorldCell(1, 0), WorldCell(1, 1), resourceToClaim), Vector())), Vector(bt))
    val action = ResourceClaim("Test", 1, WorldCell(1, 2))
    val updated = layer.tick(action)
    val expected = Vector(ResourceUnit(3, woodId))
    updated.right.value.users.head.storage.resources should be(expected)
  }

  "Sequential actions" should "spend resources" in {
    val ot = OutpostTemplate(buildingId, "Out Test", "Test res", Vector.empty, Vector(ResourceUnit(7, woodId)), 0, "")
    val bt = BuildingTemplate(buildingId, "Build Test", Vector.empty, Vector(ResourceUnit(7, woodId)), 0, shape, "")
    val user = User(1, "test", City.empty(0, 0))
    val resourceToClaim = WorldCell(1, 2, Some(Extractable(1, "Test res", 10, 111, Set.empty)))
    val userAndStorage = user.copy(storage = Storage(Vector(ResourceUnit(15, woodId))))
    val layer = Layer("", 1, Vector(userAndStorage), CellsMap(Vector(Vector(), Vector(WorldCell(1, 0), WorldCell(1, 1), resourceToClaim), Vector())), Vector(ot, bt))
    val resourceClaim = ResourceClaim("Out Test", 1, WorldCell(1, 2))
    val placeBuilding = PlaceBuilding("Build Test", layer, LocalCell(2, 1))
    val updated = layer.tick(resourceClaim)
    val lastUpdated = updated.right.value.tick(placeBuilding, userAndStorage.id)
    val expected = ResourceUnit(1, woodId)
    lastUpdated.right.value.users.head.storage.get(woodId) should be(Some(expected))
  }

}
