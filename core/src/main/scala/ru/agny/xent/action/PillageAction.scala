package ru.agny.xent.action

import ru.agny.xent.battle.Loot
import ru.agny.xent.core.User
import ru.agny.xent.core.inventory.{Cost, Item}
import ru.agny.xent.messages.{CityPillageMessage, LootResponse}

//TODO think about limits of resources to pillage
case class PillageAction(loot: Vector[Item], src: CityPillageMessage) extends UserAction {
  override def run(user: User) = {
    val s = user.city.storage
    user.spend(Cost(loot)) match {
      case Left(_) => src.respond(LootResponse(Loot(s.items))); user.spend(Cost(s.items)).right.get
      case Right(v) => src.respond(LootResponse(Loot(loot))); v
    }
  }
}
