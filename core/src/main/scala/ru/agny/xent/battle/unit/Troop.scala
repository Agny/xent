package ru.agny.xent.battle.unit

import ru.agny.xent.core.utils.UserType.{ObjectId, UserId}
import ru.agny.xent.battle.{Fatigue, MapObject, MovementPlan}
import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.unit.equip.OutcomeDamage
import ru.agny.xent.core.utils.{NESeq, SelfAware}
import ru.agny.xent.core.Coordinate

case class Troop(id: ObjectId,
                 private val units: NESeq[Soul],
                 backpack: Backpack,
                 user: UserId,
                 private val plan: MovementPlan,
                 private val fatigue: Fatigue) extends MapObject with SelfAware {

  import Fatigue._

  type Self = Troop
  override val body = units
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

  val home = plan.home

  /**
    * Method should be called if and only if this troop has able to fight units,
    * in other case UnsupportedOperationException("empty.head") will be thrown. BTW, latter can happen only due programmed logical error
    * and hence can be tested out
    */
  def attack[Target <: MapObject](other: Target): (Troop, Target) = {
    val (u, t) = activeUnits.foldLeft((Vector.empty[Soul], other))(handleBattle)
    if (!t.isActive) {
      val (looser, loot) = t.concede()
      (Troop(id, NESeq(u), backpack.add(loot)._1, user, plan, fatigue ++), looser.asInstanceOf[Target])
    } else {
      (Troop(id, NESeq(u), backpack, user, plan, fatigue ++), t)
    }
  }

  def disband(): (Int, Vector[Item]) = {
    val (power, equip) = units.foldLeft(0, Vector.empty[Item]) {
      case ((lifePower, items), soul) =>
        val (assimilatedPower, equip) = soul.beAssimilated()
        (lifePower + assimilatedPower, equip ++ items)
    }
    (power, equip ++ backpack.toLoot)
  }

  override def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]): Self = {
    val souls = activeUnits.map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
    copy(units = NESeq(souls.head, souls.tail))
  }

  override def pos(time: ProgressTime): Coordinate = {
    if (isActive) plan.now(moveSpeed, time)
    else plan.goHome(moveSpeed, time)
  }

  override def concede(): (Self, Vector[Item]) = {
    val (looserUnits, eq) = units.map(_.lose()).unzip
    val loot = backpack.toLoot ++ eq.flatMap(_.toLoot)
    val t = Troop(id, NESeq(looserUnits), Backpack.empty, user, plan, Fatigue.MAX)
    (t, loot)
  }

  private def handleBattle[Target <: MapObject](state: (Vector[Soul], Target), attacker: Soul): (Vector[Soul], Target) = {
    val (oldSate, troop) = state
    val (unitState, newTroopState) = attacker.attack(troop)
    (unitState +: oldSate, newTroopState)
  }

  override def isDiscardable = !isActive && plan.now(moveSpeed, 0) == plan.home

  override def isAggressive = isAbleToFight //TODO introduce an Order system, which defines a behaviour of the troop
}

object Troop {

  val FALLEN_SPEED = 10

  def apply(id: ObjectId, units: NESeq[Soul], backpack: Backpack, user: UserId, pos: MovementPlan): Troop = Troop(id, units, backpack, user, pos, Fatigue(0))

  def groupByUsers(troops: Iterable[MapObject]) = {
    val empty = Map.empty[UserId, Vector[MapObject]].withDefaultValue(Vector.empty)
    troops.foldLeft(empty)((m, t) => m.updated(t.user, t +: m(t.user)))
  }
}