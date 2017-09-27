package ru.agny.xent.core.city

import ru.agny.xent.core.Facility.{Idle, InConstruction, Working}
import ru.agny.xent.core._
import ru.agny.xent.core.inventory.Item._
import ru.agny.xent.core.inventory.{Extractable, ExtractionQueue, ItemStack, Obtainable}
import ru.agny.xent.core.inventory.Progress._
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.utils.ItemIdGenerator

final case class Outpost(id: ItemId,
                         c: Coordinate,
                         owner: User,
                         name: String,
                         main: Extractable,
                         obtainable: Vector[Obtainable],
                         queue: ExtractionQueue,
                         buildTime: ProgressTime,
                         state: Facility.State,
                         worker: Option[Soul] = None) extends Facility {
  //TODO think about distance/timegap

  def build = copy(state = InConstruction)

  def finish = copy(state = Idle)

  def stop: (Outpost, Option[Soul]) =
    if (isFunctioning) (copy(state = Idle, worker = None), worker)
    else (this, worker)

  def run(worker: Soul): (Outpost, Option[Soul]) =
    if (isFunctioning) (copy(state = Working, worker = Some(worker)), this.worker)
    else (this, Some(worker))

  def tick(period: ProgressTime) = {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val items = prod.map(x => ItemStack(x._2, x._1.id))
      (copy(queue = q), items)
    } else {
      (this, Vector.empty)
    }
  }

  def isFunctioning: Boolean = state == Working || state == Idle
}

object Outpost {
  def apply(c: Coordinate, owner: User, name: String, main: Extractable, obtainable: Vector[Obtainable], buildTime: ProgressTime): Outpost =
    Outpost(ItemIdGenerator.next, c, owner, name, main, obtainable, ExtractionQueue(main), buildTime, Facility.Init)
}
