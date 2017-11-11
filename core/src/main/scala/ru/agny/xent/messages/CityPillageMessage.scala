package ru.agny.xent.messages

import ru.agny.xent.action.PillageAction
import ru.agny.xent.battle.Loot
import ru.agny.xent.core.utils.ErrorCode
import ru.agny.xent.core.utils.UserType.{ItemWeight, UserId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CityPillageMessage(user: UserId, layer: String, maxLootWeight: ItemWeight) extends ActiveMessage {
  override type ResponseType = LootResponse
  override val action: PillageAction = PillageAction(maxLootWeight, this)

  var received: Future[Loot] = Future.never

  override def respond(value: LootResponse) = {
    received = Future(value.value)
    Future(value)
  }

  override def failed(code: ErrorCode.Value): Unit = Future {
    println(s"FAILED: $this-$code")
    //TODO lookup codes and construct message
  }
}
