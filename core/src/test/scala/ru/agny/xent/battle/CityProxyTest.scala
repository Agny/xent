package ru.agny.xent.battle

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.city.Storage
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.unit.{Level, Soul, SoulData, Stats}
import ru.agny.xent.core.unit.Characteristic.Strength
import ru.agny.xent.core.unit.equip.{Equipment, StatProperty}
import ru.agny.xent.core.utils.NESeq
import ru.agny.xent.core.{CellsMap, Coordinate, Layer, LayerRuntime}
import ru.agny.xent.messages.MessageQueue

class CityProxyTest extends FlatSpec with Matchers with EitherValues {

  import ru.agny.xent.core.inventory.Item.implicits._
  import ru.agny.xent.TestHelper._

  val user = defaultUser()
  val (woodId, copperId) = (1, 2)
  val layerId = "UserActionTest"
  val pos = MovementPlan.idle(Coordinate(1, 1))
  val powerfulSoul = Soul(2, SoulData(Level(1, 1), 10, Stats(Vector(StatProperty(Strength, Level(5, 0)))), Vector.empty), Equipment.empty)

  "CityProxy" should "return loot with weight around winner carry power" in {
    val cityResources = Vector(ItemStack(9, woodId, defaultWeight), ItemStack(1, copperId, 20))
    val userAndStorage = user.copy(city = user.city.copy(storage = Storage(cityResources)))
    val layer = Layer(layerId, 1, Vector(userAndStorage), Military.empty, CellsMap(Vector.empty))
    val troop = Troop(1, NESeq(Vector(powerfulSoul)), Backpack.empty, 2, pos)
    LayerRuntime.run(Vector(layer), MessageQueue.global)
    val proxy = CityProxy(user.id, layerId, Vector.empty, pos.home)
    val (_, loot) = proxy.concede(troop)

    loot.get.map(_.weight).sum should (be > 0 and be <= troop.carryPower)
  }

}
