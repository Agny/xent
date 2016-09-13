package ru.agny.xent.core

import ru.agny.xent.{ResourceUnit, Response}
import ru.agny.xent.core.Progress.ProductionTime

trait Facility extends DelayableItem {
  val name: String
  val resources: Seq[Resource]
  protected var progress: ProductionTime = 0
  //TODO common progress for queued and simple items
  protected var queue: ProductionQueue = ProductionQueue.empty()

  def tick(fromTime: ProductionTime): Storage => Storage = storage => {
    if (queue.isEmpty) {
      resources.collect { case x: Simple => x }.foldRight(storage)((res, s) =>
        s.add(extract(res, System.currentTimeMillis() - fromTime + progress, ResourceUnit(0, res.name)))
      )
    } else {
      queueTick(fromTime, storage)
    }
  }

  protected def queueTick(fromTime: ProductionTime, storage: Storage): Storage = {
    val (updatedQueue, production) = queue.out(fromTime)
    queue = updatedQueue
    storage.add(production.map(x => ResourceUnit(x._2, x._1.name)))
  }

  def addToQueue(item: ResourceUnit): Storage => Either[Response, Storage] = storage => {
    resources.find(_.name == item.res) match {
      case Some(v: Producible) =>
        storage.spend(Recipe(v, v.price(item.value))) match {
          case Left(s) => Left(s)
          case Right(s) => queue = queue.in(v, item.value); Right(s)
        }
      case _ => Left(Response(s"Facility $name cannot produce ${item.res}"))
    }
  }

  protected def extract(res: Resource, reminded: ProductionTime, extracted: ResourceUnit): ResourceUnit = {
    res match {
      case x: Finite if x.volume == 0 => extracted
      case x => extract_rec(res, reminded, extracted)
    }
  }

  private def extract_rec(res: Resource, reminded: ProductionTime, extracted: ResourceUnit): ResourceUnit = {
    if (reminded < res.yieldTime) {
      progress = reminded
      extracted
    } else {
      extract_rec(res, reminded - res.yieldTime, ResourceUnit(extracted.value + res.out().value, extracted.res))
    }
  }
}

case class Building(name: String, resources: Seq[Resource], yieldTime: ProductionTime) extends Facility
case class Outpost(name: String, main: Extractable, resources: Seq[Resource], yieldTime: ProductionTime) extends Facility {

  override def tick(fromTime: ProductionTime): Storage => Storage = storage => {
    if (queue.isEmpty) {
      storage.add(extract(main, System.currentTimeMillis() - fromTime + progress, ResourceUnit(0, main.name)))
    } else {
      queueTick(fromTime, storage)
    }
  }
}

object Facility {
  sealed trait State
  case object InConstruction extends State
  case object InProcess extends State
  case object Idle extends State
  val states = Seq(InConstruction, InProcess, Idle)
}