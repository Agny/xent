package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.core.OutcomeDamage

case class Troop(units: Seq[Soul]) {

  def attack(other: Troop): (Troop, Troop) = {
    val (u, t) = units.foldLeft((Seq.empty[Soul], other))(handleBattle)
    (Troop(u), t)
  }

  def receiveDamage(d: OutcomeDamage): Troop = receiveDamage(d, Seq(units.head.id))

  def receiveDamage(d: OutcomeDamage, targeted: Seq[ObjectId]): Troop = Troop {
    units.map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
  }

  private def handleBattle(state: (Seq[Soul], Troop), attacker: Soul): (Seq[Soul], Troop) = {
    val (unitState, newTroopState) = attacker.attack(state._2)
    (state._1 :+ unitState, newTroopState)
  }

}
