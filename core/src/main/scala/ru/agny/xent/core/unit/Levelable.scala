package ru.agny.xent.core.unit

trait Levelable

case class Level(value: Int, exp: Int) {
  val capacity: Int = Level.capacity(value)
}
object Level {
  //TODO game balancing
  def capacity(lvl: Int): Int = lvl match {
    case firstTier if lvl <= 9 => 100 + lvl * 10
    case secondTier if lvl <= 19 => 150 + lvl * 12
    case thirdTier if lvl <= 29 => 200 + lvl * 15
    case fourthTier if lvl <= 39 => lvl * 30
    case fifthTier if lvl <= 49 => lvl * 50
    case lastTier => lvl * 100
  }
}
