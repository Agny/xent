package ru.agny.xent.core

import ru.agny.xent.core.Facility.{Idle, Working}
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.inventory.{ItemStack, Obtainable, ResourceQueue}
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.utils.SelfAware

trait Facility extends Cell with Buildable {
  this: SelfAware =>
  override val weight = Int.MaxValue
  val self: Self
  val obtainable: Vector[Obtainable]
  val queue: ResourceQueue
  val worker: Option[Soul]

  def isFunctioning: Boolean = state == Working || state == Idle

  def stop: (Self, Option[Soul]) =
    if (isFunctioning) (apply(Idle, None), worker)
    else (self, worker)

  def run(worker: Soul): (Self, Option[Soul]) =
    if (isFunctioning) (apply(Working, Some(worker)), this.worker)
    else (self, Some(worker))

  def tick(period: ProgressTime): (Self, Vector[ItemStack])

  def apply(state: Facility.State, worker: Option[Soul]): Self
}

object Facility {
  sealed trait State
  case object Init extends State
  case object InConstruction extends State
  case object Working extends State
  case object Idle extends State
  case object Demolished extends State
}