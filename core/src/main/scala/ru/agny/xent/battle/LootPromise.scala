package ru.agny.xent.battle

import ru.agny.xent.messages.CityPillageMessage

import scala.util.Success

case class LootPromise(private val msg: CityPillageMessage) extends Loot {
  private val waitingLimit = 1000 * 2

  override def get = if (limitedWait(1)) msg.received.value match {
    case Some(Success(v)) => v.get
    case _ => Vector.empty
  } else Vector.empty

  private def limitedWait(waited: Int): Boolean = {
    if (msg.received.isCompleted) true
    else if (waited < waitingLimit) {
      Thread.sleep(40)
      limitedWait(waited + 40)
    } else false
  }


}
