package ru.agny.xent.core

import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.inventory.{ItemStack, Obtainable, ResourceQueue}
import ru.agny.xent.core.unit.Soul

trait Facility extends Cell with Buildable {
  val obtainable: Vector[Obtainable]
  val queue: ResourceQueue
  val worker: Option[Soul]

  def stop: (Facility, Option[Soul])

  def run(worker: Soul): (Facility, Option[Soul])

  def tick(period: ProgressTime): (Facility, Vector[ItemStack])
}

object Facility {
  sealed trait State
  case object Init extends State
  case object InConstruction extends State
  case object Working extends State
  case object Idle extends State
  case object Demolished extends State

  val states = Vector(InConstruction, Working, Idle, Init)
}