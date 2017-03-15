package ru.agny.xent.core

import ru.agny.xent.ResourceUnit

sealed trait Resource extends DelayableItem {
  val name: String
  val since: Set[Prereq]
  val defaultYield = 1

  def out(): ResourceUnit

  override def toString = s"$name"
}

trait Simple {
  self: Resource =>
}
trait Finite extends Simple {
  self: Resource =>
  var volume: Int
}
trait Composite extends Cost {
  self: Resource =>
}

case class Extractable(name: String, var volume: Int, yieldTime: Long, since: Set[Prereq]) extends Resource with Finite {
  override def out(): ResourceUnit = {
    val resultYield = if (volume > 0) defaultYield else 0
    volume = volume - resultYield
    ResourceUnit(resultYield, this.name)
  }

  override def toString = s"$name[$volume]"
}
case class Obtainable(name: String, yieldTime: Long, since: Set[Prereq]) extends Resource with Simple {
  override def out(): ResourceUnit = ResourceUnit(defaultYield, this.name)
}
trait Producible extends Resource with Composite {
  val schema:ProductionSchema
  lazy val yieldTime = schema.yieldTime
  lazy val cost = schema.cost
  lazy val since = schema.since
  def out(): ResourceUnit = ResourceUnit(defaultYield, this.name)
}
object Producible {
  private case class ProdInner(name: String, schema: ProductionSchema) extends Producible
  def apply(name: String, schema: ProductionSchema): Producible =
    ProdInner(name, schema)
}