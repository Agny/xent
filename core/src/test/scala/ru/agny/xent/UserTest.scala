package ru.agny.xent

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.core.utils.{OutpostTemplate, BuildingTemplate}
import ru.agny.xent.core._

class UserTest extends FlatSpec with Matchers with EitherValues {
  val shape = FourShape(LocalCell(0,0))

  "User" should "spend resources" in {
    val user = User(1, "test", City.empty(0, 0), Map.empty, Storage(Seq(ResourceUnit(10, "Wood")), Seq.empty), ProductionQueue.empty(), 0)
    val bt = BuildingTemplate("Test", Seq.empty, Seq(ResourceUnit(7, "Wood")), 0, shape, "")
    val updated = user.spend(bt)
    val expected = Storage(Seq(ResourceUnit(3, "Wood")), Seq.empty)
    updated.right.value.storage should be(expected)
  }

  it should "not spend any resources if there is not enough" in {
    val user = User(1, "test", City.empty(0, 0), Map.empty, Storage(Seq(ResourceUnit(5, "Wood")), Seq.empty), ProductionQueue.empty(), 0)
    val bt = BuildingTemplate("Test", Seq.empty, Seq(ResourceUnit(7, "Wood")), 0, shape, "")
    val updated = user.spend(bt)
    updated.isLeft should be(true)
  }

  "Newly created user" should "spend resources" in {
    val user = User(1, "test", City.empty(0, 0))
    val userAndStorage = user.copy(storage = Storage(Seq(ResourceUnit(10, "Wood")), user.storage.producers))
    val bt = BuildingTemplate("Test", Seq.empty, Seq(ResourceUnit(7, "Wood")), 0, shape, "")
    val updated = userAndStorage.spend(bt)
    val expected = Seq(ResourceUnit(3, "Wood"))
    updated.right.value.storage.resources should be(expected)
  }

  "PlaceBuildingAction" should "spend resources" in {
    val bt = BuildingTemplate("Test", Seq.empty, Seq(ResourceUnit(7, "Wood")), 0, shape, "")
    val layer = Layer("", 1, Seq.empty, CellsMap(Seq.empty), Seq(bt))
    val user = User(1, "test", City.empty(0, 0))
    val action = PlaceBuilding("Test", layer, LocalCell(2, 1))
    val userAndStorage = user.copy(storage = Storage(Seq(ResourceUnit(10, "Wood")), user.storage.producers))
    val updated = userAndStorage.work(action)
    val expected = Seq(ResourceUnit(3, "Wood"))
    updated.right.value.storage.resources should be(expected)
  }

  "PlaceBuildingAction" should "add building to city" in {
    val buildingConstructionTime = 10
    val bt = BuildingTemplate("Test", Seq.empty, Seq(ResourceUnit(7, "Wood")), buildingConstructionTime, shape, "")
    val layer = Layer("", 1, Seq.empty, CellsMap(Seq.empty), Seq(bt))
    val user = User(1, "test", City.empty(0, 0))
    val bCell = LocalCell(2, 1)
    val action = PlaceBuilding("Test", layer, bCell)
    val userAndStorage = user.copy(storage = Storage(Seq(ResourceUnit(10, "Wood")), user.storage.producers))
    val updated = userAndStorage.work(action).right.value

    Thread.sleep(buildingConstructionTime)
    val userWithBuilding = updated.work(DoNothing).right.value
    val expected = bt.name
    val updatedCell = userWithBuilding.city.buildings().find(c => c.x == bCell.x && c.y == bCell.y)
    updatedCell.isEmpty shouldBe false
    updatedCell.get.building.isEmpty shouldBe false
    updatedCell.get.building.get.name should be (expected)
  }

  "ResourceClaimAction" should "spend resources" in {
    val bt = OutpostTemplate("Test", "Test res", Seq.empty, Seq(ResourceUnit(7, "Wood")), 0, "")
    val user = User(1, "test", City.empty(0, 0))
    val resourceToClaim = WorldCell(1, 2, Some(Extractable("Test res", 10, 111, Set.empty)))
    val userAndStorage = user.copy(storage = Storage(Seq(ResourceUnit(10, "Wood")), user.storage.producers))
    val layer = Layer("", 1, Seq(userAndStorage), CellsMap(Seq(Seq(), Seq(WorldCell(1, 0), WorldCell(1, 1), resourceToClaim), Seq())), Seq(bt))
    val action = ResourceClaim("Test", 1, WorldCell(1, 2))
    val updated = layer.tick(action)
    val expected = Seq(ResourceUnit(3, "Wood"))
    updated.right.value.users.head.storage.resources should be(expected)
  }

  "Sequential actions" should "spend resources" in {
    val ot = OutpostTemplate("Out Test", "Test res", Seq.empty, Seq(ResourceUnit(7, "Wood")), 0, "")
    val bt = BuildingTemplate("Build Test", Seq.empty, Seq(ResourceUnit(7, "Wood")), 0, shape, "")
    val user = User(1, "test", City.empty(0, 0))
    val resourceToClaim = WorldCell(1, 2, Some(Extractable("Test res", 10, 111, Set.empty)))
    val userAndStorage = user.copy(storage = Storage(Seq(ResourceUnit(15, "Wood")), user.storage.producers))
    val layer = Layer("", 1, Seq(userAndStorage), CellsMap(Seq(Seq(), Seq(WorldCell(1, 0), WorldCell(1, 1), resourceToClaim), Seq())), Seq(ot, bt))
    val resourceClaim = ResourceClaim("Out Test", 1, WorldCell(1, 2))
    val placeBuilding = PlaceBuilding("Build Test", layer, LocalCell(2, 1))
    val updated = layer.tick(resourceClaim)
    val lastUpdated = updated.right.value.tick(placeBuilding, userAndStorage.id)
    val expected = ResourceUnit(1, "Wood")
    lastUpdated.right.value.users.head.storage.get("Wood") should be(Some(expected))
  }

}
