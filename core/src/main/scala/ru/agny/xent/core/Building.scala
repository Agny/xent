package ru.agny.xent.core

import ru.agny.xent.battle.unit.Soul
import ru.agny.xent.core.Item._
import ru.agny.xent.core.Progress._

case class Building(id: ItemId,
                    name: String,
                    producibles: Vector[Producible],
                    obtainables: Vector[Obtainable],
                    queue: ResourceQueue,
                    buildTime: ProgressTime,
                    shape: Shape,
                    state: Facility.State,
                    worker: Option[Soul] = None) extends Facility {
  override protected def instance(queue: ResourceQueue, state: Facility.State, worker: Option[Soul]): Facility = copy(queue = queue, state = state, worker = worker)
}

object Building {
  def apply(id: ItemId, name: String, producibles: Vector[Producible], yieldTime: ProgressTime, shape: Shape): Building =
    Building(id, name, producibles, Vector.empty, ProductionQueue.empty, yieldTime, shape, Facility.Init)
}
