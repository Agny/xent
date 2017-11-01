package ru.agny.xent.messages

import ru.agny.xent.battle.Loot

case class PlainResponse(value: String) extends Response {
  override type T = String
}
case class LootResponse(value: Loot) extends Response {
  override type T = Loot
}
object ResponseOk extends PlainResponse("Ok")
