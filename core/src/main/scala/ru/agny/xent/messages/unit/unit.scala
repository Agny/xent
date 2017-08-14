package ru.agny.xent.messages

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.unit.Spirit
import ru.agny.xent.persistence.RedisEntity

package object unit {

  @RedisEntity("user", "user", System.nanoTime().toString)
  case class CreateSoulMessage(user: UserId, layer: String, baseSpirit: Spirit, stats: Vector[StatPropertySimple]) extends Message
  @RedisEntity("user", "user", System.nanoTime().toString)
  case class CreateTroopMessage(user: UserId, layer: String, souls: Vector[Long]) extends Message

}
