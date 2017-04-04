package ru.agny.xent.core

import ru.agny.xent.Response
import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.Progress.ProductionTime

sealed trait Facility extends DelayableItem {
  val name: String
  val resources: Seq[Resource]
  val queue: ProductionQueue
  val state: Facility.State

  def tick(fromTime: ProductionTime): Storage => Storage = storage => {
    val (q, prod) = queue.out(fromTime)
    storage.updateProducer(this, instance(q)).add(prod.map(x => ResourceUnit(x._2, x._1.id)))
  }

  def addToQueue(item: ResourceUnit): Storage => Either[Response, Storage] = storage => {
    resources.find(_.id == item.id) match {
      case Some(v: Producible) =>
        storage.spend(Recipe(v, v.price(item.stackValue))) match {
          case Left(s) => Left(s)
          case Right(s) => Right(s.updateProducer(this, instance(queue.in(v, item.stackValue))))
        }
      case _ => Left(Response(s"Facility $name cannot produce ${item.id}"))
    }
  }

  protected def instance(queue: ProductionQueue): Facility
}

final case class Building(id: ItemId,
                          name: String,
                          resources: Seq[Resource],
                          queue: ProductionQueue,
                          yieldTime: ProductionTime,
                          shape: Shape,
                          state: Facility.State = Facility.Init) extends Facility {
  override protected def instance(queue: ProductionQueue): Facility = copy(queue = queue)
}
final case class Outpost(id: ItemId,
                         name: String,
                         main: Extractable,
                         resources: Seq[Resource],
                         queue: ProductionQueue,
                         yieldTime: ProductionTime,
                         state: Facility.State = Facility.Init) extends Facility {
  override protected def instance(queue: ProductionQueue): Facility = copy(queue = queue)
}

object Facility {
  sealed trait State
  case object InConstruction extends State
  case object InProcess extends State
  case object Idle extends State
  case object Init extends State

  val states = Seq(InConstruction, InProcess, Idle, Init)
}

object Building {
  def apply(id: ItemId, name: String, resources: Seq[Resource], yieldTime: ProductionTime, shape: Shape): Building =
    Building(id, name, resources, ProductionQueue.empty(), yieldTime, shape)
}

object Outpost {
  def apply(id: ItemId, name: String, main: Extractable, resources: Seq[Resource], yieldTime: ProductionTime): Outpost =
    Outpost(id, name, main, resources, ProductionQueue.empty(), yieldTime)
}