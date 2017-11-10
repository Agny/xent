package ru.agny.xent.battle

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.unit.equip.OutcomeDamage
import ru.agny.xent.core.utils.SelfAware
import ru.agny.xent.core.utils.UserType.{ObjectId, UserId}
import ru.agny.xent.messages.CityPillageMessage
import ru.agny.xent.messages.MessageQueue._

case class CityProxy(user: UserId, layer: String, items: Vector[Item], souls: Vector[Soul], pos: Coordinate) extends MapObject with SelfAware {
  override type Self = CityProxy
  override val id = user
  override val body = souls
  override val weight = body.map(_.weight).sum

  override def pos(time: ProgressTime) = pos

  override def isActive = body.exists(_.spirit > 0)

  override def isAbleToFight = isActive

  override def isDiscardable = false

  override def isAggressive = false

  override def concede() = {
    val msg = CityPillageMessage(user, layer, items)
    global.push(msg)
    val loot = LootPromise(items, msg)
    (copy(items = Vector.empty), loot)
  }

  override def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]) = {
    val souls = body.filter(_.spirit > 0).map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
    copy(souls = souls)
  }
}
