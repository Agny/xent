package ru.agny.xent.core

import ru.agny.xent.core.Item.ItemId

sealed trait Resource extends DelayableItem with SingleItem {
  val name: String
  val since: Set[Prereq]
  val defaultYield = 1
  //TODO usecases are lost!
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

case class Extractable(id: ItemId, name: String, var volume: Int, yieldTime: Long, since: Set[Prereq]) extends Resource with Finite {
  override def out(): ResourceUnit = {
    val resultYield = if (volume > 0) defaultYield else 0
    volume = volume - resultYield
    ResourceUnit(resultYield, id)
  }

  override def toString = s"$name[$volume]"
}
case class Obtainable(id: ItemId, name: String, yieldTime: Long, since: Set[Prereq]) extends Resource with Simple {
  override def out(): ResourceUnit = ResourceUnit(defaultYield, id)
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
case class Producible(id: ItemId, name: String, cost: Vector[ResourceUnit], yieldTime: Long, since: Set[Prereq]) extends Resource with Composite {
  override def out(): ResourceUnit = ResourceUnit(defaultYield, id)
}