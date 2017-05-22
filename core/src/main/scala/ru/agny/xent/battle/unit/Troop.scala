package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.{UserId, ObjectId}
import ru.agny.xent.battle.core.OutcomeDamage
import ru.agny.xent.battle.unit.inventory.Backpack
import ru.agny.xent.core.{Item, Coordinate}

case class Troop(id: ObjectId, private val units: Vector[Soul], backpack: Backpack, user: UserId, pos: Coordinate, fatigue: Fatigue) {

  import Fatigue._

  val activeUnits = units.filter(_.state == Soul.Active)
  lazy val weight = activeUnits.foldLeft(0)((sum, x) => sum + x.weight)
  lazy val isActive = activeUnits.nonEmpty

  lazy val endurance = (activeUnits match {
    case x@h +: t => x.minBy(_.endurance).endurance
    case _ => 0
  }) - fatigue

  val moveSpeed = activeUnits match {
    case x@h +: t => x.minBy(_.speed).speed
    case _ => 0
  }

  lazy val initiative = if (activeUnits.nonEmpty)
    activeUnits.map(_.initiative).sum / activeUnits.length
  else 0

  def attack(other: Troop): (Troop, Troop) = {
    val (u, t) = activeUnits.foldLeft((Vector.empty[Soul], other))(handleBattle)
    if (t.activeUnits.isEmpty) {
      val (looser, loot) = t.concede()
      (Troop(id, u, backpack.add(loot)._1, user, pos, fatigue ++), looser)
    } else {
      (Troop(id, u, backpack, user, pos, fatigue ++), t)
    }
  }

  def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]): Troop = {
    val souls = activeUnits.map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
    copy(units = souls)
  }

  private def concede(): (Troop, Vector[Item]) = {
    val (looserUnits, eq) = units.map(_.lose()).unzip
    val loot = backpack.toSpoil ++ eq.flatMap(_.toSpoil)
    val t = Troop(id, looserUnits, Backpack.empty, user, pos, Fatigue.MAX)
    (t, loot)
  }

  private def handleBattle(state: (Vector[Soul], Troop), attacker: Soul): (Vector[Soul], Troop) = {
    val (oldSate, troop) = state
    val (unitState, newTroopState) = attacker.attack(troop)
    (unitState +: oldSate, newTroopState)
  }

}

case class Fatigue(v: Int) {
  def ++ = Fatigue(v + 1)
}

object Troop {

  def apply(id: ObjectId, units: Vector[Soul], backpack: Backpack, user: UserId, pos: Coordinate): Troop = Troop(id, units, backpack, user, pos, Fatigue(0))

  def groupByUsers(troops: Iterable[Troop]) = {
    val empty = Map.empty[UserId, Vector[Troop]].withDefaultValue(Vector.empty)
    troops.foldLeft(empty)((m, t) => m.updated(t.user, t +: m(t.user)))
  }
}

object Fatigue {
  val MAX = Fatigue(Int.MaxValue)

  implicit def toInt(f: Fatigue): Int = f.v
}