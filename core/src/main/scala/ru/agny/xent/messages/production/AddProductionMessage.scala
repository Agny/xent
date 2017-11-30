package ru.agny.xent.messages.production

import ru.agny.xent.action.AddProduction
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.ReactiveLog
import ru.agny.xent.persistence.redis.RedisEntity

@RedisEntity("user", "user", System.nanoTime().toString)
case class AddProductionMessage(user: UserId, layer: String, facility: ItemId, res: ItemStack) extends ReactiveLog {
  override val action = AddProduction(facility, res, this)
}
