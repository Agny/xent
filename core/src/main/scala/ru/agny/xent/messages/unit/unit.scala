package ru.agny.xent.messages

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.unit.equip.StatProperty
import ru.agny.xent.core.unit.{Characteristic, Level, Spirit}
import ru.agny.xent.persistence.RedisEntity

package object unit {

  @RedisEntity("user", "user", System.nanoTime().toString)
  case class CreateSoulMessage(user: UserId, layer: String, baseSpirit: Spirit, stats: Vector[StatPropertySimple]) extends Message

  case class StatPropertySimple(prop: Characteristic, level: Int) {
    def lift = StatProperty(prop, Level(level, 0))
  }

}
