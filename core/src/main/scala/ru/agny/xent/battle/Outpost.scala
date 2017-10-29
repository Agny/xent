package ru.agny.xent.battle

import ru.agny.xent.battle.unit.{Cargo, Guard}
import ru.agny.xent.core.Facility.{Demolished, Working}
import ru.agny.xent.core._
import ru.agny.xent.core.inventory.Item._
import ru.agny.xent.core.inventory.Progress._
import ru.agny.xent.core.inventory.{Extractable, ExtractionQueue, ItemStack, Obtainable}
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.unit.equip.OutcomeDamage
import ru.agny.xent.core.utils.UserType.ObjectId
import ru.agny.xent.core.utils.{ItemIdGenerator, NESeq, SelfAware}

final case class Outpost(id: ItemId,
                         c: Coordinate,
                         owner: User,
                         name: String,
                         main: Extractable,
                         obtainable: Vector[Obtainable],
                         queue: ExtractionQueue,
                         buildTime: ProgressTime,
                         state: Facility.State,
                         worker: Option[Soul] = None,
                         body: NESeq[Guard],
                         stored: Vector[ItemStack]) extends MapObject with Facility with UnitSpawner with SelfAware {

  override type Self = Outpost
  override val self = this
  override val user = owner.id
  override val weight = Outpost.battleWeight + body.map(_.weight).sum

  def tick(period: ProgressTime) = {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val items = prod.map(x => ItemStack(x._2, x._1.id))
      (copy(queue = q, stored = store(items)), items)
    } else {
      (this, Vector.empty)
    }
  }

  def isCargoReady = stored.nonEmpty

  private def store(mined: Vector[ItemStack]): Vector[ItemStack] =
    mined.map(x => stored.find(_.id == x.id).map(y => ItemStack(x.stackValue + y.stackValue, x.id)).getOrElse(x))

  override def pos(time: ProgressTime) = c

  override def isActive = isFunctioning && body.exists(_.spirit > 0)

  override def isAbleToFight = false

  override def isDiscardable = state == Demolished

  override def isAggressive = false

  override def concede() = (copy(stored = Vector.empty, state = Demolished), Loot(stored))

  override def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]) = {
    val souls = body.filter(_.spirit > 0).map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
    copy(body = NESeq(souls.head, souls.tail))
  }

  override def spawn: (Self, Cargo) = {
    val guards = NESeq(Guard.tiered(0)(owner.id) +: Vector.empty)
    val movePlan = MovementPlan(Vector(Movement(c, owner.city.c)), owner.city.c)
    val cargo = Cargo(ItemIdGenerator.next, user, guards, stored, movePlan)
    (copy(stored = Vector.empty), cargo)
  }

  override def apply(state: Facility.State) = copy(state = state)

  override def apply(state: Facility.State, worker: Option[Soul]) = copy(state = state, worker = worker)
}

object Outpost {
  val battleWeight = 100

  def apply(c: Coordinate, owner: User, name: String, main: Extractable, obtainable: Vector[Obtainable], buildTime: ProgressTime): Outpost = {
    val guards = NESeq(Guard.tiered(0)(owner.id) +: Vector.empty)
    Outpost(ItemIdGenerator.next, c, owner, name, main, obtainable, ExtractionQueue(main), buildTime, Facility.Init, None, guards, Vector.empty)
  }
}
