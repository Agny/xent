package ru.agny.xent.action

import org.scalatest._
import ru.agny.xent.TestHelper.defaultUser
import ru.agny.xent.battle.Military
import ru.agny.xent.core.city.Shape.FourShape
import ru.agny.xent.core.{CellsMap, Coordinate, Layer, LifePower}
import ru.agny.xent.core.city._
import ru.agny.xent.core.inventory._
import ru.agny.xent.core.unit.characteristic.{Agility, Strength}
import ru.agny.xent.core.unit.{Spirit, SpiritBase}
import ru.agny.xent.core.utils.{BuildingTemplate, CityGenerator, TemplateProvider}
import ru.agny.xent.messages.CityPillageMessage
import ru.agny.xent.messages.production.{AddProductionMessage, BuildingConstructionMessage}
import ru.agny.xent.messages.unit.{CreateSoulMessage, StatPropertySimple}

class UserActionTest extends AsyncFlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  import ru.agny.xent.core.inventory.Item.implicits._

  val user = defaultUser()
  val shape = FourShape.name
  val (woodId, copperId) = (1, 2)
  val constructionTime = 10
  val bTemplate = BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), constructionTime, shape, "")
  val layerId = "UserActionTest"
  val layer = Layer(layerId, 1, Vector.empty, Military.empty, CellsMap(Vector.empty))

  override protected def beforeAll(): Unit = {
    TemplateProvider.add(layerId, bTemplate)
    ShapeProvider.add(BuildingTemplate("Test", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId))), 0, shape, ""))
  }

  override protected def afterAll(): Unit = {
    TemplateProvider.clear(layerId)
    ShapeProvider.delete("Test")
  }

  "PlaceBuildingAction" should "spend resources" in {
    val msg = BuildingConstructionMessage(user.id, layer.id, bTemplate.name, Coordinate(2, 1))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val updated = userAndStorage.work(msg.action)
    val expected = Vector(ItemStack(3, woodId))
    updated.city.storage.items should be(expected)
  }

  it should "add building to the city" in {
    val msg = BuildingConstructionMessage(user.id, layer.id, bTemplate.name, Coordinate(2, 1))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId)))))
    val updated = userAndStorage.work(msg.action)

    Thread.sleep(constructionTime)
    val userWithBuilding = updated.work(DoNothing)
    val mbBuilding = userWithBuilding.city.producers.find(c => c.name == bTemplate.name)
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

    val msg = AddProductionMessage(user.id, layer.id, building.id, ItemStack(prodCount, prodId))
    val afterAction = userToAct.work(msg.action)
    afterAction.city.producers.head.queue.content should be(Vector((prod, prodCount)))
  }

  "CreateSoul" should "create soul with specified parameters" in {
    val spiritPoints = 41
    val spirit = Spirit(spiritPoints, SpiritBase(2, 100))
    val props = Vector(StatPropertySimple(Agility.toString, 3), StatPropertySimple(Strength.toString, 5))
    val userWithPower = user.copy(power = LifePower(300, 300))

    val msg = CreateSoulMessage(user.id, layer.id, spirit, props)
    val afterAction = userWithPower.work(msg.action)
    val createdSoul = afterAction.souls.souls.head._1
    val soulLifepower = createdSoul.beAssimilated()._1

    createdSoul.spirit should be(spiritPoints)
    soulLifepower should be(props.map(_.lift.toLifePower).sum)
  }

  "Pillage" should "take specified resources from the city" in {
    val loot = Vector(ItemStack(2, woodId), ItemStack(3, copperId))
    val msg = CityPillageMessage(user.id, layerId, loot)
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(5, woodId), ItemStack(7, copperId)))))
    val afterAction = userAndStorage.work(msg.action)

    val expected = Vector(ItemStack(3, woodId), ItemStack(4, copperId))
    msg.received map { resultLoot =>
      resultLoot.get should contain allElementsOf loot
      afterAction.city.storage.items should be(expected)
    }
  }

  it should "take all resources from the city if specified ones unavailable" in {
    val loot = Vector(ItemStack(2, woodId), ItemStack(3, copperId))
    val msg = CityPillageMessage(user.id, layerId, loot)
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(5, woodId)))))
    val afterAction = userAndStorage.work(msg.action)

    val expected = Vector(ItemStack(5, woodId))
    msg.received map { resultLoot =>
      resultLoot.get should contain allElementsOf expected
      afterAction.city.storage.items should be(empty)
    }
  }
}
