package ru.agny.xent.core

import ru.agny.xent.core.Item._
import ru.agny.xent.core.Progress._

case class Building(id: ItemId,
                    name: String,
                    producibles: Vector[Producible],
                    obtainables: Vector[Obtainable],
                    queue: ResourceQueue,
                    buildTime: ProgressTime,
                    shape: Shape,
                    state: Facility.State = Facility.Init) extends Facility {
  override protected def instance(queue: ResourceQueue): Facility = copy(queue = queue)
}

object Building {
  def apply(id: ItemId, name: String, producibles: Vector[Producible], yieldTime: ProgressTime, shape: Shape): Building =
    Building(id, name, producibles, Vector.empty, ProductionQueue.empty, yieldTime, shape)
}
