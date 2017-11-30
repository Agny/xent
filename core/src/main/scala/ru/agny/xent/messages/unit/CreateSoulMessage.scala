package ru.agny.xent.messages.unit

import ru.agny.xent.action.CreateSoul
import ru.agny.xent.core.unit.Spirit
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.ReactiveLog
import ru.agny.xent.persistence.redis.RedisEntity

@RedisEntity("user", "user", System.nanoTime().toString)
case class CreateSoulMessage(user: UserId, layer: String, baseSpirit: Spirit, stats: Vector[StatPropertySimple]) extends ReactiveLog {
  override val action = CreateSoul(baseSpirit, stats.map(_.lift), this)
}
