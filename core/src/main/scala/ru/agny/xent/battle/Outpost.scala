package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Guard
import ru.agny.xent.core.Facility.{Demolished, Idle, InConstruction, Working}
import ru.agny.xent.core._
import ru.agny.xent.core.inventory.Item._
import ru.agny.xent.core.inventory.Progress._
import ru.agny.xent.core.inventory.{Extractable, ExtractionQueue, ItemStack, Obtainable}
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.unit.equip.OutcomeDamage
import ru.agny.xent.core.utils.UserType.ObjectId
import ru.agny.xent.core.utils.{ItemIdGenerator, NESeq}

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
                         stored: Vector[ItemStack]) extends MapObject with Facility {

  override type Self = Outpost
  // ** Facility methods block starts **

  def build = copy(state = InConstruction)

  def finish = copy(state = Idle)

  def stop: (Outpost, Option[Soul]) =
    if (isFunctioning) (copy(state = Idle, worker = None), worker)
    else (this, worker)

  def run(worker: Soul): (Outpost, Option[Soul]) =
    if (isFunctioning) (copy(state = Working, worker = Some(worker)), this.worker)
    else (this, Some(worker))

  def tick(period: ProgressTime) = {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val items = prod.map(x => ItemStack(x._2, x._1.id))
      (copy(queue = q, stored = store(items)), items)
    } else {
      (this, Vector.empty)
    }
  }

  private def store(mined: Vector[ItemStack]): Vector[ItemStack] =
    stored.map(x => mined.find(_.id == x.id).map(y => ItemStack(x.stackValue + y.stackValue, x.id)).getOrElse(x))


  def isFunctioning: Boolean = state == Working || state == Idle

  // ** Facility methods block ends **

  override val user = owner.id
  override val weight = Outpost.battleWeight + body.map(_.weight).sum

  override def pos(time: ProgressTime) = c

  override def isActive = isFunctioning && body.exists(_.spirit > 0)

  override def isAbleToFight = false

  override def isDiscardable = state == Demolished

  override def isAggressive = false

  override def concede() = (copy(stored = Vector.empty, state = Demolished), stored)

  override def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]) = {
    val souls = body.filter(_.spirit > 0).map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
    copy(body = NESeq(souls.head, souls.tail))
  }
}

object Outpost {
  val battleWeight = 100

  def apply(c: Coordinate, owner: User, name: String, main: Extractable, obtainable: Vector[Obtainable], buildTime: ProgressTime): Outpost = {
    val guards = NESeq(Guard.tiered(0)(owner.id) +: Vector.empty)
    Outpost(ItemIdGenerator.next, c, owner, name, main, obtainable, ExtractionQueue(main), buildTime, Facility.Init, None, guards, Vector.empty)
  }
}
