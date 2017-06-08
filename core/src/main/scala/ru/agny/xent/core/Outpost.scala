package ru.agny.xent.core

import ru.agny.xent.core.Item._
import ru.agny.xent.core.Progress._

final case class Outpost(id: ItemId,
                         name: String,
                         main: Extractable,
                         obtainables: Vector[Obtainable],
                         queue: ResourceQueue,
                         buildTime: ProgressTime,
                         state: Facility.State = Facility.Init) extends Facility {
  override val producibles = Vector.empty

  override protected def instance(queue: ResourceQueue): Facility = copy(queue = queue)
}

object Outpost {
  def apply(id: ItemId, name: String, main: Extractable, obtainables: Vector[Obtainable], yieldTime: ProgressTime): Outpost =
    Outpost(id, name, main, obtainables, ExtractionQueue(main), yieldTime)
}
