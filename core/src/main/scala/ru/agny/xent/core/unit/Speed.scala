package ru.agny.xent.core.unit

import ru.agny.xent.core.unit.Speed.Distance
import ru.agny.xent.core.utils.TimeUnit

object Speed {
  type Speed = Int
  type Distance = Long
  implicit class SpeedI(tilesPerHour: Speed) {
    def in(millis: Long): Distance = millis * tilesPerHour
  }

  val default = 10
}

object Distance {

  val centerOfTile = tileToDistance(1) / 2

  def tilesWithRemainder(traveled: Distance): (Int, Distance) = (math.floor(traveled / TimeUnit.hour).toInt, traveled % TimeUnit.hour)

  def tileToDistance(tiles: Int): Distance = tiles * TimeUnit.hour
}
