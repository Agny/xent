package ru.agny.xent.core

import ru.agny.xent.core.Progress.ProgressTime

case class ProductionSchema(yieldTime: ProgressTime, cost: Cost, since: Set[Prereq])
object ProductionSchema {
  def default() = ProductionSchema(0, Cost(Vector.empty), Set.empty)
}