package ru.agny.xent.unit

import ru.agny.xent.item.Equipment
import ru.agny.xent.{ItemWeight, Velocity}

//TODO Equipment boosting stats
/** @param level is required for equipping items and learning skills. Represents experience, accumulated by the soul in this incarnation */
case class SoulData(level: Level, spiritPower: Spirit) {

  def armor: Int = ???

  def spirit: Int = spiritPower.points

  def velocity: Velocity = 10

  def weight: Int = spirit

  def endurance: Int = ???

  def initiative: Int = ???

  def attackModifiers: Vector[Int] = ???

  def defenseModifiers: Int = ???

  def receiveDamage(dmg: Int): Unit = {
    spiritPower.change(-dmg)
  }

  def gainExp(amount: Int) = ???

  def carryPower: ItemWeight = ???

}

object SoulData {
  val Empty = SoulData(Level.start, Spirit(10, 10, 100))
}