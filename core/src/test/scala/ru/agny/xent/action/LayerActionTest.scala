package ru.agny.xent.action

import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper._
import ru.agny.xent.battle.{Military, Waiting}
import ru.agny.xent.core._
import ru.agny.xent.core.city.{Storage, Workers}
import ru.agny.xent.core.inventory.{Cost, Extractable, ItemStack}
import ru.agny.xent.core.utils.{LayerGenerator, OutpostTemplate, TemplateProvider}
import ru.agny.xent.messages.NewUserMessage
import ru.agny.xent.messages.production.ResourceClaimMessage
import ru.agny.xent.messages.unit.CreateTroopMessage

class LayerActionTest extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  import ru.agny.xent.core.inventory.Item.implicits._

  val user = defaultUser()
  val woodId = 1
  val layerId = "LayerActionTest"
  val bt = OutpostTemplate("Test", "Test res", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId, defaultWeight))), 0, "")

  override protected def beforeAll(): Unit = {
    TemplateProvider.add(layerId, OutpostTemplate("Test", "Test res", Vector.empty, Vector.empty, Cost(Vector(ItemStack(7, woodId, defaultWeight))), 0, ""))
  }

  override protected def afterAll(): Unit = {
    TemplateProvider.clear(layerId)
  }

  "ResourceClaimAction" should "spend resources" in {
    val place = Coordinate(1, 2)
    val resourceToClaim = ResourceCell(place, Extractable(1, "Test res", 10, 111, 12, Set.empty))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(Vector(ItemStack(10, woodId, defaultWeight)))))
    val layer = Layer(layerId, 1, Vector(userAndStorage), Military.empty, CellsMap(Vector(Vector(), Vector(Cell(1, 0), Cell(1, 1), resourceToClaim), Vector())))
    val msg = ResourceClaimMessage(user.id, layer.id, bt.name, place)
    val updated = layer.tick(msg.action)
    val expected = Vector(ItemStack(3, woodId, defaultWeight))
    updated.users.head.city.storage.items should be(expected)
  }

  "CreateTroop" should "add troop to layer" in {
    val (soulOne, soulTwo) = (defaultSoul(1), defaultSoul(2))
    val souls = Vector(soulOne, soulTwo).map(_ -> new Waiting(user.city.c))
    val userWithSouls = user.copy(souls = Workers(souls))
    val layer = Layer(layerId, 1, Vector(userWithSouls), Military.empty, LayerGenerator.generateWorldMap(3, Vector.empty))
    val msg = CreateTroopMessage(user.id, layer.id, Vector(soulOne.id, soulTwo.id))
    val layerWithTroop = layer.tick(msg.action)

    layerWithTroop.armies.objects should not be Vector.empty
  }

  "NewUser" should "add new user and city" in {
    val layer = Layer("", 1, Vector.empty, Military.empty, LayerGenerator.generateWorldMap(3, Vector.empty))
    val msg = NewUserMessage(user.id, "Test", layer.id)
    val withNewUser = layer.tick(msg.action)

    withNewUser.users should not be Vector.empty
    withNewUser.armies.objects should not be Vector.empty
  }
}
