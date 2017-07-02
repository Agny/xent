package ru.agny.xent.core.unit

import ru.agny.xent.core.utils.TimeUnit

object Speed {
  type Speed = Int
  implicit class SpeedI(tilesPerHour: Speed) {
    def in(millis: Long): Int = math.floor(millis * tilesPerHour / TimeUnit.hour).toInt
  }
}
