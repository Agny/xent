package ru.agny.xent.core

import ru.agny.xent.core.city.{Building, Outpost}
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.inventory.{DelayableItem, ItemStack, Obtainable, ResourceQueue}
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.utils.SubTyper

trait Facility extends Cell with DelayableItem {
  val obtainable: Vector[Obtainable]
  val queue: ResourceQueue
  val buildTime: ProgressTime
  override val yieldTime = buildTime
  val state: Facility.State
  val worker: Option[Soul]

  def build: Facility

  def finish: Facility

  def stop: (Facility, Option[Soul])

  def run(worker: Soul): (Facility, Option[Soul])

  def tick(period: ProgressTime): (Facility, Vector[ItemStack])
}

object Facility {
  sealed trait State
  private[core] case object Init extends State
  private[core] case object InConstruction extends State
  private[core] case object Working extends State
  private[core] case object Idle extends State
  private[core] case object Demolished extends State

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