package ru.agny.xent.unit

import ru.agny.xent.{ItemId, ItemWeight}
import ru.agny.xent.item.{Equipment, Equippable, MapObject}
import ru.agny.xent.realm.map.Troops

case class Soul(id: ItemId, private val stats: SoulData, private val equip: Equipment) {

  import Soul._
  
  def carryPower() = stats.carryPower

  def weight(): ItemWeight = stats.weight

  def endurance() = stats.endurance

  def initiative() = stats.initiative

  def velocity() = stats.velocity

  def spirit() = stats.spirit

  def state() = spirit() match {
    case alive if alive > 0 => Soul.State.Active
    case _ => Soul.State.Fallen
  }

  def attackRates(using target: MapObject) = stats.attackModifiers

  def attack(target: Troops): Unit = {
    target.takeDamage(DefaultDamage)
  }

  //TODO damage attributes
  def takeDamage(damage: Int): Unit = {
    stats.receiveDamage(damage)
  }

  def gainExp(amount: Int): Unit = stats.gainExp(amount)

  // soul spirit points are ignored when life power is being calculated
  def becomeUndone(): (Int, Vector[Equippable]) = ???
}

object Soul {

  val DefaultDamage = 10
  val Empty = Soul(-1, SoulData.Empty, Equipment.Empty)

  enum State {
    case Active
    case Fallen
  }
}
