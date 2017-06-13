package ru.agny.xent.core

import ru.agny.xent.Response
import Progress.ProgressTime
import ru.agny.xent.battle.unit.Soul
import ru.agny.xent.core.Facility.{InConstruction, Idle, Working}
import ru.agny.xent.core.utils.SubTyper

trait Facility extends DelayableItem {
  val obtainables: Vector[Obtainable]
  val producibles: Vector[Producible]
  val queue: ResourceQueue
  val buildTime: ProgressTime
  override val yieldTime = buildTime
  //TODO state transitions
  val state: Facility.State
  val worker: Option[Soul]

  def tick(period: ProgressTime): Storage => (Storage, Facility) = storage => {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val (s, excess) = storage.add(prod.map(x => ItemStack(x._2, x._1.id)))
      (s, instance(q, state, worker))
    } else {
      (storage, this)
    }
  }

  def addToQueue(item: ItemStack): Storage => Either[Response, (Storage, Facility)] = storage => {
    producibles.find(_.id == item.id) match {
      case Some(v: Producible) =>
        storage.spend(v.cost.price(item.stackValue)) match {
          case Left(s) => Left(s)
          case Right(s) => Right((s, instance(queue.in(v, item.stackValue), state, worker)))
        }
      case _ => Left(Response(s"Facility $name cannot produce ${item.id}"))
    }
  }

  def build = instance(queue, InConstruction, worker)

  def finish = instance(queue, Idle, worker)

  def stop: (Facility, Option[Soul]) = {
    state match {
      case Idle | Working => (instance(queue, Idle, None), worker)
      case _ => (this, worker)
    }
  }

  def run(worker: Soul): (Facility, Option[Soul]) = {
    state match {
      case Idle | Working => (instance(queue, Working, Some(worker)), this.worker)
      case _ => (this, Some(worker))
    }
  }

  protected def instance(queue: ResourceQueue, state: Facility.State, worker: Option[Soul]): Facility
}

object Facility {
  sealed trait State
  case object Init extends State
  private case object InConstruction extends State
  private case object Working extends State
  private case object Idle extends State

  val states = Vector(InConstruction, Working, Idle, Init)
}

object FacilitySubTyper {
  object implicits {
    implicit object BuildingMatcher extends SubTyper[Facility, Building] {
      override def asSub(a: Facility): Option[Building] = a match {
        case a: Building => Some(a)
        case _ => None
      }
    }

    implicit object OutpostMatcher extends SubTyper[Facility, Outpost] {
      override def asSub(a: Facility): Option[Outpost] = a match {
        case a: Outpost => Some(a)
        case _ => None
      }
    }
  }
}