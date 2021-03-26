package ru.agny.xent.unit

import ru.agny.xent.{ItemId, ItemWeight}
import ru.agny.xent.item.{Equipment, Equippable, MapObject}

case class Soul(id: ItemId, private val stats: SoulData, private val equip: Equipment) {

  def carryPower() = stats.carryPower
  def weight(): ItemWeight = stats.weight
  def endurance() = stats.endurance
  def initiative() = stats.initiative
  def velocity() = stats.velocity
  def spirit() = stats.spirit
  def state() = spirit() match {
    case alive if alive > 0 => SoulState.Active
    case _ => SoulState.Fallen
  }

  def attackRates(using target: MapObject) = stats.attackModifiers

  def attack[Target <: MapObject](target: Target): Unit = ???

  def gainExp(amount: Int): Unit = stats.gainExp(amount)

  // soul spirit points are ignored when life power is being calculated
  def becomeUndone(): (Int, Vector[Equippable]) = ???
}

enum SoulState {
  case Active
  case Fallen
}
