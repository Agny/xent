package ru.agny.xent.action

import ru.agny.xent.battle.Loot
import ru.agny.xent.core.User
import ru.agny.xent.core.inventory.Cost
import ru.agny.xent.messages.{CityPillageMessage, LootResponse}

//TODO think about limits of resources to pillage
case class PillageAction(loot: Loot, src: CityPillageMessage) extends UserAction {
  override def run(user: User) = {
    val s = user.city.storage
    user.spend(Cost(loot.get)) match {
      case Left(_) => src.respond(LootResponse(Loot(s.resources))); user.spend(Cost(s.resources)).right.get
      case Right(v) => src.respond(LootResponse(loot)); v
    }
  }
}
