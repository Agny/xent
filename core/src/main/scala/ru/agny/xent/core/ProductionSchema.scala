package ru.agny.xent.core

case class ProductionSchema(yieldTime: Long, cost: Vector[ResourceUnit], since: Set[Prereq])
object ProductionSchema {
  def default() = ProductionSchema(0, Vector.empty, Set.empty)
}