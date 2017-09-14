package ru.agny.xent.core.inventory

import ru.agny.xent.core.inventory.Item._

trait Producible extends DelayableItem {
  val schema: ProductionSchema
  lazy val yieldTime = schema.yieldTime
  lazy val cost = schema.cost
  lazy val since = schema.since
}
object Producible {
  private case class ProdInner(id: ItemId, name: String, schema: ProductionSchema) extends Producible
  def apply(id: ItemId, name: String, schema: ProductionSchema): Producible = ProdInner(id, name, schema)
}
