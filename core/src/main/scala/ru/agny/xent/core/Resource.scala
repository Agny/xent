package ru.agny.xent.core

sealed trait Resource {
  val name: String
  val since: Set[Prereq]
  val yieldTime: Long
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
case class Producible(name: String, cost: List[ResourceUnit], yieldTime: Long, since: Set[Prereq]) extends Resource with Composite {
  override def out(): ResourceUnit = ResourceUnit(defaultYield, this.name)
}