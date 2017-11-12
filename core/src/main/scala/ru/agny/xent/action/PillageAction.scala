package ru.agny.xent.action

import ru.agny.xent.battle.Loot
import ru.agny.xent.core.User
import ru.agny.xent.core.inventory.Item.ItemWeight
import ru.agny.xent.messages.{CityPillageMessage, LootResponse}

case class PillageAction(weightLimit: ItemWeight, src: CityPillageMessage) extends UserAction {
  override def run(user: User) = {
    val (updated, loot) = user.loseResources(weightLimit)
    src.respond(LootResponse(Loot(loot)))
    updated
  }
}
