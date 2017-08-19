package ru.agny.xent.core.unit

import ru.agny.xent.core.utils.TimeUnit

object Speed {
  type Speed = Int
  type Distance = Long
  implicit class SpeedI(tilesPerHour: Speed) {
    def in(millis: Long): Distance = millis * tilesPerHour
  }
  implicit class DistanceI(traveled: Distance) {
    def tiles: Int = math.floor(traveled / TimeUnit.hour).toInt
  }

  val default = 10
}
