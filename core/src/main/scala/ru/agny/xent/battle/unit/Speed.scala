package ru.agny.xent.battle.unit

object Speed {
  implicit class SpeedI(tilesPerHour: Int) {
    def in(millis: Long): Int = math.floor(millis * tilesPerHour / (1000 * 60 * 60)).toInt
  }
}
