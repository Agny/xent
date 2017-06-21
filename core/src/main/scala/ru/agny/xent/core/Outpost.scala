package ru.agny.xent.core

import ru.agny.xent.battle.unit.Soul
import ru.agny.xent.core.Facility.{Working, Idle, InConstruction}
import ru.agny.xent.core.Item._
import ru.agny.xent.core.Progress._
import ru.agny.xent.core.utils.ItemIdGenerator

final case class Outpost(id: ItemId,
                         name: String,
                         main: Extractable,
                         obtainables: Vector[Obtainable],
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

  def tick(period: ProgressTime): Storage => (Storage, Outpost) = storage => {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val (s, excess) = storage.add(prod.map(x => ItemStack(x._2, x._1.id)))
      (s, copy(queue = q))
    } else {
      (storage, this)
    }
  }

  def isFunctioning: Boolean = state == Working || state == Idle
}

object Outpost {
  def apply(name: String, main: Extractable, obtainables: Vector[Obtainable], buildTime: ProgressTime): Outpost =
    Outpost(ItemIdGenerator.next, name, main, obtainables, ExtractionQueue(main), buildTime, Facility.Init)
}
