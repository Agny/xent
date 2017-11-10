package ru.agny.xent.battle

import ru.agny.xent.core.inventory.Item
import ru.agny.xent.messages.CityPillageMessage

import scala.util.Success

case class LootPromise(private val mbLoot: Vector[Item], private val validatorMsg: CityPillageMessage) extends Loot {
  def isValidated: Boolean = validatorMsg.received.isCompleted

  override def get = if (isValidated)
    validatorMsg.received.value match {
      case Some(Success(v)) => v.get
      case _ => mbLoot
    }
  else Vector.empty

}
