package ru.agny.xent.messages

import ru.agny.xent.battle.Loot

case class PlainResponse(value: String) extends Response[String]
case class LootResponse(value: Loot) extends Response[Loot]
object ResponseOk extends PlainResponse("Ok")
