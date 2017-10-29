package ru.agny.xent.action

import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper._
import ru.agny.xent.battle.{Military, Waiting}
import ru.agny.xent.core._
import ru.agny.xent.core.city.{Storage, Workers}
import ru.agny.xent.core.inventory.{Cost, Extractable, ItemStack}
import ru.agny.xent.core.utils.{LayerGenerator, OutpostTemplate}

class LayerActionTest extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  import ru.agny.xent.core.inventory.Item.implicits._

  val user = defaultUser()
  val woodId = 1

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

  "CreateTroop" should "add troop to layer" in {
    val (soulOne, soulTwo) = (defaultSoul(1), defaultSoul(2))
    val souls = Vector(soulOne, soulTwo).map(_ -> new Waiting(user.city.c))
    val userWithSouls = user.copy(souls = Workers(souls))
    val layer = Layer("", 1, Vector(userWithSouls), Military.empty, LayerGenerator.generateWorldMap(3, Vector.empty), Vector())
    val layerWithTroop = layer.tick(CreateTroop(user.id, Vector(soulOne.id, soulTwo.id))).right.value

    layerWithTroop.armies.objects should not be Vector.empty
  }

  "NewUser" should "add new user and city" in {
    val layer = Layer("", 1, Vector.empty, Military.empty, LayerGenerator.generateWorldMap(3, Vector.empty), Vector())
    val withNewUser = layer.tick(NewUser(100, "Test")).right.value

    withNewUser.users should not be Vector.empty
    withNewUser.armies.objects should not be Vector.empty
  }
}
