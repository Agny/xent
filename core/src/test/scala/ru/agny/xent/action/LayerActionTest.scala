package ru.agny.xent.action

import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper.defaultUser
import ru.agny.xent.battle.Military
import ru.agny.xent.core._
import ru.agny.xent.core.city.Storage
import ru.agny.xent.core.inventory.{Cost, Extractable, ItemStack}
import ru.agny.xent.core.utils.OutpostTemplate

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
}
