package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.{ObjectId, UserId}
import ru.agny.xent.battle.{Fatigue, MovementPlan}
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.unit.{Soul, Speed}
import ru.agny.xent.core.unit.equip.OutcomeDamage
import ru.agny.xent.core.utils.NESeq
import ru.agny.xent.core.{Coordinate, Item}

case class Troop(id: ObjectId, private val units: NESeq[Soul], backpack: Backpack, user: UserId, private val pos: MovementPlan, fatigue: Fatigue) {

  import Fatigue._

  val activeUnits = units.filter(_.state == Soul.Active)
  lazy val weight = activeUnits.foldLeft(0)((sum, x) => sum + x.weight)
  lazy val isActive = activeUnits.nonEmpty
  lazy val isAbleToFight = isActive && endurance > 0

  lazy val endurance = (activeUnits match {
    case x@h +: t => x.minBy(_.endurance).endurance
    case _ => 0
  }) - fatigue

  val moveSpeed = activeUnits match {
    case x@h +: t => x.minBy(_.speed).speed
    case _ => Troop.FALLEN_SPEED
  }

  lazy val initiative = if (activeUnits.nonEmpty)
    activeUnits.map(_.initiative).sum / activeUnits.length
  else 0

  val home = pos.home

  /**
    * Method should be called if and only if this troop has able to fight units,
    * in other case UnsupportedOperationException("empty.head") will be thrown. BTW, latter can happen only due programmed logical error
    * and hence can be tested out
    */
  def attack(other: Troop): (Troop, Troop) = {
    val (u, t) = activeUnits.foldLeft((Vector.empty[Soul], other))(handleBattle)
    if (t.activeUnits.isEmpty) {
      val (looser, loot) = t.concede()
      (Troop(id, NESeq(u), backpack.add(loot)._1, user, pos, fatigue ++), looser)
    } else {
      (Troop(id, NESeq(u), backpack, user, pos, fatigue ++), t)
    }
  }

  def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]): Troop = {
    val souls = activeUnits.map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
    copy(units = NESeq(souls.head, souls.tail))
  }

  def move(time: ProgressTime): Coordinate = {
    if (isActive) pos.now(moveSpeed, time)
    else pos.goHome(moveSpeed, time)
  }

  private def concede(): (Troop, Vector[Item]) = {
    val (looserUnits, eq) = units.map(_.lose()).unzip
    val loot = backpack.toSpoil ++ eq.flatMap(_.toSpoil)
    val t = Troop(id, NESeq(looserUnits), Backpack.empty, user, pos, Fatigue.MAX)
    (t, loot)
  }

  private def handleBattle(state: (Vector[Soul], Troop), attacker: Soul): (Vector[Soul], Troop) = {
    val (oldSate, troop) = state
    val (unitState, newTroopState) = attacker.attack(troop)
    (unitState +: oldSate, newTroopState)
  }
}

object Troop {

  val FALLEN_SPEED = 10

  def apply(id: ObjectId, units: NESeq[Soul], backpack: Backpack, user: UserId, pos: MovementPlan): Troop = Troop(id, units, backpack, user, pos, Fatigue(0))

  def groupByUsers(troops: Iterable[Troop]) = {
    val empty = Map.empty[UserId, Vector[Troop]].withDefaultValue(Vector.empty)
    troops.foldLeft(empty)((m, t) => m.updated(t.user, t +: m(t.user)))
  }
}