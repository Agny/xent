package ru.agny.xent.messages

import ru.agny.xent.action.PillageAction
import ru.agny.xent.battle.Loot
import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.ErrorCode
import ru.agny.xent.core.utils.UserType.UserId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CityPillageMessage(user: UserId, layer: String, loot: Vector[Item]) extends ActiveMessage {
  override type ResponseType = LootResponse
  override val action: PillageAction = PillageAction(loot, this)

  var received: Future[Loot] = Future.never

  override def respond(value: LootResponse) = {
    received = Future(value.value)
    Future(value)
  }

  override def failed(code: ErrorCode.Value): Unit = ???
}
