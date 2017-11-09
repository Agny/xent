package ru.agny.xent.messages.production

import ru.agny.xent.action.ResourceClaim
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.ReactiveLog
import ru.agny.xent.persistence.RedisEntity

@RedisEntity("user", "user", System.nanoTime().toString)
case class ResourceClaimMessage(user: UserId, layer: String, facility: String, cell: Coordinate) extends ReactiveLog {
  override val action = ResourceClaim(facility, user, cell, this)
}

