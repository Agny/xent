package ru.agny.xent.battle.unit

object Speed {
  type Speed = Int
  val hour = 1000 * 60 * 60
  implicit class SpeedI(tilesPerHour: Speed) {
    def in(millis: Long): Int = math.floor(millis * tilesPerHour / hour).toInt
  }
}
