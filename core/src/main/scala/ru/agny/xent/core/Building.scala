package ru.agny.xent.core

import ru.agny.xent.Response
import ru.agny.xent.battle.unit.Soul
import ru.agny.xent.core.Facility.{Working, Idle, InConstruction}
import ru.agny.xent.core.Item._
import ru.agny.xent.core.Progress._

case class Building(id: ItemId,
                    name: String,
                    producibles: Vector[Producible],
                    obtainables: Vector[Obtainable],
                    queue: ProductionQueue,
                    buildTime: ProgressTime,
                    shape: Shape,
                    state: Facility.State,
                    worker: Option[Soul] = None) extends Facility {
  def build = copy(state = InConstruction)

  def finish = copy(state = Idle)

  def stop: (Building, Option[Soul]) = state match {
    case Idle | Working => (copy(state = Idle, worker = None), worker)
    case _ => (this, worker)
  }

  def run(worker: Soul): (Building, Option[Soul]) = state match {
    case Idle | Working => (copy(state = Working, worker = Some(worker)), this.worker)
    case _ => (this, Some(worker))
  }

  def tick(period: ProgressTime): Storage => (Storage, Building) = storage => {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val (s, excess) = storage.add(prod.map(x => ItemStack(x._2, x._1.id)))
      (s, copy(queue = q))
    } else {
      (storage, this)
    }
  }

  def addToQueue(item: ItemStack): Storage => Either[Response, (Storage, Building)] = storage => {
    producibles.find(_.id == item.id) match {
      case Some(v: Producible) =>
        storage.spend(v.cost.price(item.stackValue)) match {
          case Left(s) => Left(s)
          case Right(s) => Right((s, copy(queue = queue.in(v, item.stackValue))))
        }
      case _ => Left(Response(s"Facility $name cannot produce ${item.id}"))
    }
  }
}

object Building {
  def apply(id: ItemId, name: String, producibles: Vector[Producible], buildTime: ProgressTime, shape: Shape): Building =
    Building(id, name, producibles, Vector.empty, ProductionQueue.empty, buildTime, shape, Facility.Init)
}
