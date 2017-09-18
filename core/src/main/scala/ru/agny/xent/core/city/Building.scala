package ru.agny.xent.core.city

import ru.agny.xent.core.Facility.{Idle, InConstruction, Working}
import ru.agny.xent.core.inventory.Item._
import ru.agny.xent.core.inventory.Progress._
import ru.agny.xent.core._
import ru.agny.xent.core.inventory.{ItemStack, Obtainable, Producible, ProductionQueue}
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.utils.ItemIdGenerator
import ru.agny.xent.messages.Response

case class Building(id: ItemId,
                    c: Coordinate,
                    name: String,
                    producible: Vector[Producible],
                    obtainable: Vector[Obtainable],
                    queue: ProductionQueue,
                    buildTime: ProgressTime,
                    state: Facility.State,
                    worker: Option[Soul] = None) extends Facility {
  def build = copy(state = InConstruction)

  def finish = copy(state = Idle)

  def stop: (Building, Option[Soul]) =
    if (isFunctioning) (copy(state = Idle, worker = None), worker)
    else (this, worker)

  def run(worker: Soul): (Building, Option[Soul]) =
    if (isFunctioning) (copy(state = Working, worker = Some(worker)), this.worker)
    else (this, Some(worker))

  def tick(period: ProgressTime): Storage => (Storage, Building) = storage => {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val (s, excess) = storage.add(prod.map(x => ItemStack(x._2, x._1.id)))
      (s, copy(queue = q))
    } else {
      (storage, this)
    }
  }

  def isFunctioning: Boolean = state == Working || state == Idle

  def addToQueue(item: ItemStack): Storage => Either[Response, (Storage, Building)] = storage => {
    producible.find(_.id == item.id) match {
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
  def apply(c: Coordinate, name: String, producible: Vector[Producible], buildTime: ProgressTime): Building =
    Building(ItemIdGenerator.next, c, name, producible, Vector.empty, ProductionQueue.empty, buildTime, Facility.Init)
}
