package ru.agny.xent.battle.unit

object Speed {
  type Speed = Int
  implicit class SpeedI(tilesPerHour: Speed) {
    def in(millis: Long): Int = math.floor(millis * tilesPerHour / (1000 * 60 * 60)).toInt
  }
}
