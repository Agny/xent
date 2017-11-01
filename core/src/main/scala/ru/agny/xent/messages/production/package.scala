package ru.agny.xent.messages

import ru.agny.xent.action.{AddProduction, PlaceBuilding, ResourceClaim}
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.persistence.RedisEntity

package object production {

  @RedisEntity("user", "user", System.nanoTime().toString)
  case class ResourceClaimMessage(user: UserId, layer: String, facility: String, cell: Coordinate) extends ReactiveLog {
    override val action = ResourceClaim(facility, user, cell, this)
  }

  @RedisEntity("user", "user", System.nanoTime().toString)
  case class BuildingConstructionMessage(user: UserId, layer: String, building: String, cell: Coordinate) extends ReactiveLog {
    override val action = PlaceBuilding(building, cell, this)
  }

  @RedisEntity("user", "user", System.nanoTime().toString)
  case class AddProductionMessage(user: UserId, layer: String, facility: ItemId, res: ItemStack) extends ReactiveLog {
    override val action = AddProduction(facility, res, this)
  }

}
