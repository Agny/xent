package ru.agny.xent.core

import ru.agny.xent.core.Item.ItemId

sealed trait Resource extends DelayableItem with SingleItem {
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
case class Producible(id: ItemId, name: String, cost: Seq[ResourceUnit], yieldTime: Long, since: Set[Prereq]) extends Resource with Composite {
  override def out(): ResourceUnit = ResourceUnit(defaultYield, id)
}