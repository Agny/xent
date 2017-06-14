package ru.agny.xent.core

import ru.agny.xent.battle.unit.Soul
import ru.agny.xent.core.Facility.{Working, Idle, InConstruction}
import ru.agny.xent.core.Item._
import ru.agny.xent.core.Progress._

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

  def stop: (Outpost, Option[Soul]) = state match {
    case Idle | Working => (copy(state = Idle, worker = None), worker)
    case _ => (this, worker)
  }

  def run(worker: Soul): (Outpost, Option[Soul]) = state match {
    case Idle | Working => (copy(state = Working, worker = Some(worker)), this.worker)
    case _ => (this, Some(worker))
  }

  def tick(period: ProgressTime): Storage => (Storage, Outpost) = storage => {
    if (state == Working) {
      val (q, prod) = queue.out(period)
      val (s, excess) = storage.add(prod.map(x => ItemStack(x._2, x._1.id)))
      (s, copy(queue = q))
    } else {
      (storage, this)
    }
  }
}

object Outpost {
  def apply(id: ItemId, name: String, main: Extractable, obtainables: Vector[Obtainable], yieldTime: ProgressTime): Outpost =
    Outpost(id, name, main, obtainables, ExtractionQueue(main), yieldTime, Facility.Init)
}
