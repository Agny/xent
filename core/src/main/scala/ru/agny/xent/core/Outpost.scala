package ru.agny.xent.core

import ru.agny.xent.battle.unit.Soul
import ru.agny.xent.core.Item._
import ru.agny.xent.core.Progress._

final case class Outpost(id: ItemId,
                         name: String,
                         main: Extractable,
                         obtainables: Vector[Obtainable],
                         queue: ResourceQueue,
                         buildTime: ProgressTime,
                         state: Facility.State,
                         worker: Option[Soul] = None) extends Facility {
  override val producibles = Vector.empty

  override protected def instance(queue: ResourceQueue, state: Facility.State, worker: Option[Soul]): Facility = copy(queue = queue, state = state, worker = worker)
}

object Outpost {
  def apply(id: ItemId, name: String, main: Extractable, obtainables: Vector[Obtainable], yieldTime: ProgressTime): Outpost =
    Outpost(id, name, main, obtainables, ExtractionQueue(main), yieldTime, Facility.Init)
}
