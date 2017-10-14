package ru.agny.xent.action

import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper.defaultUser
import ru.agny.xent.battle.Military
import ru.agny.xent.core.city.Shape.FourShape
import ru.agny.xent.core.{CellsMap, Coordinate, Layer}
import ru.agny.xent.core.city.{ShapeProvider, Storage}
import ru.agny.xent.core.inventory.{Cost, ItemStack}
import ru.agny.xent.core.utils.BuildingTemplate

class UserActionTest extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  import ru.agny.xent.core.inventory.Item.implicits._

  val user = defaultUser()
  val shape = FourShape.name
  val woodId = 1

  override protected def beforeAll(): Unit = {
    ShapeProvider.add(BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, ""))
  }

  override protected def afterAll(): Unit = {
    ShapeProvider.delete("Test")
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

}
