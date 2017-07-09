package ru.agny.xent.core.unit

//TODO add mechanic to gain experience
trait Levelable

case class Level(value: Int, exp: Int) {
  val capacity: Int = Level.capacity(value)

  def tiered: Seq[(Int, Int)] = {
    val tiers = value / 10
    val lvlInLastTier = value % 10
    1 to tiers + 1 map (x => x -> (if (x > tiers) lvlInLastTier else 10))
  }
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
