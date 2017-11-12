package ru.agny.xent.core.inventory

import ru.agny.xent.core.Prereq
import ru.agny.xent.core.inventory.Item.ItemWeight
import ru.agny.xent.core.inventory.Progress.ProgressTime

case class ProductionSchema(yieldTime: ProgressTime, cost: Cost, weight: ItemWeight, since: Set[Prereq])
object ProductionSchema {
  def default() = ProductionSchema(0, Cost(Vector.empty), 1, Set.empty)
}