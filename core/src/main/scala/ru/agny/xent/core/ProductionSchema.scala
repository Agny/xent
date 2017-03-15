package ru.agny.xent.core

import ru.agny.xent.ResourceUnit

case class ProductionSchema(yieldTime: Long, cost: Seq[ResourceUnit], since: Set[Prereq])
object ProductionSchema {
  def default() = ProductionSchema(0, Seq.empty, Set.empty)
}