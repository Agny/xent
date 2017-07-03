package ru.agny.xent.core.unit

trait Levelable

case class Level(value: Int, exp: Int, capacity: Int)
object Level {
  //TODO game balancing
  def nextCapacity(lastCap: Int): Int = lastCap * 15 / 10
}
