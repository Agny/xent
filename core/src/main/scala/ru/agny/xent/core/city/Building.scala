package ru.agny.xent.core.city

import ru.agny.xent.core.Facility.Working
import ru.agny.xent.core.inventory.Item._
import ru.agny.xent.core.inventory.Progress._
import ru.agny.xent.core._
import ru.agny.xent.core.inventory.{ItemStack, Obtainable, Producible, ProductionQueue}
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.utils.{ItemIdGenerator, SelfAware}
import ru.agny.xent.messages.Response

case class Building(id: ItemId,
                    c: Coordinate,
                    name: String,
                    producible: Vector[Producible],
                    obtainable: Vector[Obtainable],
                    queue: ProductionQueue,
                    buildTime: ProgressTime,
                    state: Facility.State,
                    worker: Option[Soul] = None) extends Facility with SelfAware {
  override type Self = Building
  override val self = this

  def tick(period: ProgressTime): (Building, Vector[ItemStack]) = {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val items = prod.map(x => ItemStack(x._2, x._1.id))
      (copy(queue = q), items)
    } else {
      (this, Vector.empty)
    }
  }

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

  override def apply(state: Facility.State) = copy(state = state)

  override def apply(state: Facility.State, worker: Option[Soul]) = copy(state = state, worker = worker)
}

object Building {
  def apply(c: Coordinate, name: String, producible: Vector[Producible], buildTime: ProgressTime): Building =
    Building(ItemIdGenerator.next, c, name, producible, Vector.empty, ProductionQueue.empty, buildTime, Facility.Init)
}
