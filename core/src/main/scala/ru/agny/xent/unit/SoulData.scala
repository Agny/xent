package ru.agny.xent.unit

import ru.agny.xent.item.Equipment
import ru.agny.xent.{ItemWeight, Velocity}

//TODO Equipment boosting stats
/** @param level is required for equipping items and learning skills. Represents experience, accumulated by the soul in this incarnation */
case class SoulData() {

  def armor: Int = ???

  def spirit: Int = ???

  def velocity: Velocity = ???

  def weight: Int = ???

  def endurance: Int = ???

  def initiative: Int = ???

  def attackModifiers: Vector[Int] = ???

  def defenseModifiers: Int = ???

  def receiveDamage(dmg: Int): SoulData = ???

  def gainExp(amount: Int) = ???

  def carryPower: ItemWeight = ???

}