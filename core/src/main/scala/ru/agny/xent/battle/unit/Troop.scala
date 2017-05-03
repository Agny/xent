package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.{UserId, ObjectId}
import ru.agny.xent.battle.core.OutcomeDamage
import ru.agny.xent.battle.unit.inventory.Backpack

case class Troop(id: ObjectId)(val units: Vector[Soul], val backpack: Backpack, user: UserId) {

  def attack(other: Troop): (Troop, Troop) = {
    val (u, t) = units.foldLeft((Vector.empty[Soul], other))(handleBattle)
    if (t.units.isEmpty) {
      val (withLoot, _) = backpack.add(t.backpack.toSpoil)
      (Troop(id)(u, withLoot, user), Troop(t.id)(t.units, Backpack.empty, user))
    } else {
      (Troop(id)(u, backpack, user), t)
    }
  }

  def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]): Troop = {
    val (alive, fallenEquip) = units.map {
      case u if targeted.contains(u.id) =>
        val damaged = u.receiveDamage(d)
        if (damaged.spirit.points <= 0) (None, damaged.equip.toSpoil)
        else (Some(damaged), Vector.empty)
      case unharmed => (Some(unharmed), Vector.empty)
    }.unzip
    if (alive.flatten.isEmpty) {
      // TODO troop is defeated, send souls
    }
    Troop(id)(alive.flatten, backpack.add(fallenEquip.flatten)._1, user)
  }

  private def handleBattle(state: (Vector[Soul], Troop), attacker: Soul): (Vector[Soul], Troop) = {
    val (oldSate, troop) = state
    val (unitState, newTroopState) = attacker.attack(troop)
    (unitState +: oldSate, newTroopState)
  }

}
