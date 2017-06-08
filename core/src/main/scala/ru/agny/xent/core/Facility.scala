package ru.agny.xent.core

import ru.agny.xent.Response
import Item.ItemId
import Progress.ProgressTime

sealed trait Facility extends DelayableItem {
  val obtainables: Vector[Obtainable]
  val producibles: Vector[Producible]
  val queue: ResourceQueue
  val buildTime: ProgressTime
  override val yieldTime = buildTime
  //TODO state transitions
  val state: Facility.State

  def tick(fromTime: ProgressTime): Storage => (Storage, Facility) = storage => {
    val (q, prod) = queue.out(fromTime)
    val (s, excess) = storage.add(prod.map(x => ItemStack(x._2, x._1.id)))
    (s, instance(q))
  }

  def addToQueue(item: ItemStack): Storage => Either[Response, (Storage, Facility)] = storage => {
    producibles.find(_.id == item.id) match {
      case Some(v: Producible) =>
        storage.spend(v.cost.price(item.stackValue)) match {
          case Left(s) => Left(s)
          case Right(s) => Right((s, instance(queue.in(v, item.stackValue))))
        }
      case _ => Left(Response(s"Facility $name cannot produce ${item.id}"))
    }
  }

  protected def instance(queue: ResourceQueue): Facility
}

final case class Building(id: ItemId,
                          name: String,
                          producibles: Vector[Producible],
                          obtainables: Vector[Obtainable],
                          queue: ResourceQueue,
                          buildTime: ProgressTime,
                          shape: Shape,
                          state: Facility.State = Facility.Init) extends Facility {
  override protected def instance(queue: ResourceQueue): Facility = copy(queue = queue)
}
final case class Outpost(id: ItemId,
                         name: String,
                         main: Extractable,
                         obtainables: Vector[Obtainable],
                         queue: ResourceQueue,
                         buildTime: ProgressTime,
                         state: Facility.State = Facility.Init) extends Facility {
  override val producibles = Vector.empty

  override protected def instance(queue: ResourceQueue): Facility = copy(queue = queue)
}

object Facility {
  sealed trait State
  case object InConstruction extends State
  case object InProcess extends State
  case object Idle extends State
  case object Init extends State

  val states = Vector(InConstruction, InProcess, Idle, Init)
}

object Building {
  def apply(id: ItemId, name: String, producibles: Vector[Producible], yieldTime: ProgressTime, shape: Shape): Building =
    Building(id, name, producibles, Vector.empty, ProductionQueue.empty, yieldTime, shape)
}

object Outpost {
  def apply(id: ItemId, name: String, main: Extractable, obtainables: Vector[Obtainable], yieldTime: ProgressTime): Outpost =
    Outpost(id, name, main, obtainables, ExtractionQueue(main), yieldTime)
}