package ru.agny.xent.messages.unit

import ru.agny.xent.action.CreateTroop
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.ReactiveLog
import ru.agny.xent.persistence.RedisEntity

@RedisEntity("user", "user", System.nanoTime().toString)
case class CreateTroopMessage(user: UserId, layer: String, souls: Vector[Long]) extends ReactiveLog {
  override val action = CreateTroop(user, souls, this)
}
