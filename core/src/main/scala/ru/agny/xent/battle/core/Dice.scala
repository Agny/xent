package ru.agny.xent.battle.core

import scala.util.Random

case class Dice(count: Int, sides: Int) extends Ordered[Dice] {
  private val rnd = Random

  def cast: Int = (1 to count).foldLeft(0)((acc, _) => acc + (rnd.nextInt(sides) + 1))

  override def compare(that: Dice): Int = count * sides - that.count * that.sides
}

object Dice {
  implicit class DiceI(count: Int) {
    def d(sides: Int): Dice = Dice(count, sides)
  }
}
