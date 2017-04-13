package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.core.OutcomeDamage

case class Troop(units: Vector[Soul]) {

  def attack(other: Troop): (Troop, Troop) = {
    val (u, t) = units.foldLeft((Vector.empty[Soul], other))(handleBattle)
    (Troop(u), t)
  }

  def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]): Troop = Troop {
    units.map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
  }

  private def handleBattle(state: (Vector[Soul], Troop), attacker: Soul): (Vector[Soul], Troop) = {
    val (oldSate, troop) = state
    val (unitState, newTroopState) = attacker.attack(troop)
    (unitState +: oldSate, newTroopState)
  }

}
