package ru.agny.xent.messages

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.{Coordinate, ItemStack}
import ru.agny.xent.persistence.RedisEntity

package object production {

  @RedisEntity("user", "user", System.nanoTime().toString)
  case class ResourceClaimMessage(user: UserId, layer: String, facility: String, cell: Coordinate) extends Message

  @RedisEntity("user", "user", System.nanoTime().toString)
  case class BuildingConstructionMessage(user: UserId, layer: String, building: String, cell: Coordinate) extends Message

  @RedisEntity("user", "user", System.nanoTime().toString)
  case class AddProductionMessage(user: UserId, layer: String, facility: ItemId, res: ItemStack) extends Message

}
