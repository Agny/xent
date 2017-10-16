package ru.agny.xent.action

import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper.defaultUser
import ru.agny.xent.battle.Military
import ru.agny.xent.core.city.Shape.FourShape
import ru.agny.xent.core.{CellsMap, Coordinate, Layer, LifePower}
import ru.agny.xent.core.city._
import ru.agny.xent.core.inventory._
import ru.agny.xent.core.unit.characteristic.{Agility, Strength}
import ru.agny.xent.core.unit.equip.StatProperty
import ru.agny.xent.core.unit.{Characteristic, Level, Spirit, SpiritBase}
import ru.agny.xent.core.utils.{BuildingTemplate, CityGenerator}
import ru.agny.xent.messages.unit.StatPropertySimple

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
    val constructionTime = 10
    val bt = BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), constructionTime, shape, "")
    val layer = Layer("", 1, Vector.empty, Military.empty, CellsMap(Vector.empty), Vector(bt))
    val action = PlaceBuilding(bt.name, layer, Coordinate(2, 1))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val updated = userAndStorage.work(action).right.value

    Thread.sleep(constructionTime)
    val userWithBuilding = updated.work(DoNothing).right.value
    val mbBuilding = userWithBuilding.city.producers.find(c => c.name == bt.name)
    mbBuilding.isEmpty shouldBe false
  }

  "AddProduction" should "update user production queue" in {
    val prodId = 2
    val prodCount = 5
    val prod = Producible(prodId, "Coal", ProductionSchema(100, Cost(ItemStack(2, woodId)), Set.empty))
    val building = Building(Coordinate(3, 3), "Furnace", Vector(prod), 100).finish
    val cityMap = CityGenerator.generateCityMap(3).update(building)

    val withBuilding = City(Coordinate(12, 12), ShapeMap(cityMap, Vector.empty), Storage(Vector(ItemStack(10, woodId))))
    val userToAct = user.copy(city = withBuilding)

    val afterAction = userToAct.work(AddProduction(building.id, ItemStack(prodCount, prodId))).right.value
    afterAction.city.producers.head.queue.content should be(Vector((prod, prodCount)))
  }

  "CreateSoul" should "create soul with specified parameters" in {
    val spiritPoints = 41
    val spirit = Spirit(spiritPoints, SpiritBase(2, 100))
    val props = Vector(StatProperty(Agility, Level(3, 11)), StatProperty(Strength, Level(5, 0)))
    val userWithPower = user.copy(power = LifePower(300, 300))

    val afterAction = userWithPower.work(CreateSoul(spirit, props)).right.value
    val createdSoul = afterAction.souls.souls.head._1
    val soulLifepower = createdSoul.beAssimilated()._1

    createdSoul.spirit should be(spiritPoints)
    soulLifepower should be(props.map(_.toLifePower).sum)
  }

}
