package ru.agny.xent.core

import ru.agny.xent.{ResourceUnit, Response}
import ru.agny.xent.core.Progress.ProductionTime

trait Facility extends DelayableItem {
  val name: String
  val resources: Seq[Resource]
  val queue: ProductionQueue

  def tick(fromTime: ProductionTime): Storage => Storage = storage => {
    val (q, prod) = queue.out(fromTime)
    storage.updateProducer(this, instance(q)).add(prod.map(x => ResourceUnit(x._2, x._1.name)))
  }

  def addToQueue(item: ResourceUnit): Storage => Either[Response, Storage] = storage => {
    resources.find(_.name == item.res) match {
      case Some(v: Producible) =>
        storage.spend(Recipe(v, v.price(item.value))) match {
          case Left(s) => Left(s)
          case Right(s) => Right(s.updateProducer(this, instance(queue.in(v, item.value))))
        }
      case _ => Left(Response(s"Facility $name cannot produce ${item.res}"))
    }
  }

  protected def instance(queue: ProductionQueue): Facility
}

case class Building(name: String, resources: Seq[Resource], queue: ProductionQueue, yieldTime: ProductionTime, shape: Shape) extends Facility {
  override protected def instance(queue: ProductionQueue): Facility = copy(queue = queue)
}
case class Outpost(name: String, main: Extractable, resources: Seq[Resource], queue: ProductionQueue, yieldTime: ProductionTime) extends Facility {
  override protected def instance(queue: ProductionQueue): Facility = copy(queue = queue)
}

object Facility {
  sealed trait State
  case object InConstruction extends State
  case object InProcess extends State
  case object Idle extends State
  val states = Seq(InConstruction, InProcess, Idle)
}

object Building {
  def apply(name: String, resources: Seq[Resource], yieldTime: ProductionTime, shape: Shape): Building =
    Building(name, resources, ProductionQueue.empty(), yieldTime, shape)
}

object Outpost {
  def apply(name: String, main: Extractable, resources: Seq[Resource], yieldTime: ProductionTime): Outpost =
    Outpost(name, main, resources, ProductionQueue.empty(), yieldTime)
}