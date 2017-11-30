package ru.agny.xent.messages.production

import ru.agny.xent.action.PlaceBuilding
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.ReactiveLog
import ru.agny.xent.persistence.redis.RedisEntity

@RedisEntity("user", "user", System.nanoTime().toString)
case class BuildingConstructionMessage(user: UserId, layer: String, building: String, cell: Coordinate) extends ReactiveLog {
  override val action = PlaceBuilding(building, cell, this)
}
