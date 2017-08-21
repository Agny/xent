package ru.agny.xent.core.unit

import ru.agny.xent.core.utils.TimeUnit

object Speed {
  type Speed = Int
  type Distance = Long
  implicit class SpeedI(tilesPerHour: Speed) {
    def in(millis: Long): Distance = millis * tilesPerHour
  }

  def tilesWithRemainder(traveled: Distance): (Int, Distance) = (math.floor(traveled / TimeUnit.hour).toInt, traveled % TimeUnit.hour)

  def tileToDistance(tiles: Int): Distance = tiles * TimeUnit.hour

  val default = 10
}
