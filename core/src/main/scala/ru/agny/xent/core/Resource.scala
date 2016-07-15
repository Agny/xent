package ru.agny.xent.core

sealed trait Resource {
  val name: String
  val since: Set[Prereq]
  val yieldTime: Long
  val defaultYield = 1

  def out(): ResourceUnit

  override def toString = s"$name"
}
case class Extractable(id: Long, name: String, var volume: Int, yieldTime: Long, since: Set[Prereq]) extends Resource {
  override def out(): ResourceUnit = {
    val resultYield = if (volume > 0) defaultYield else 0
    volume = volume - resultYield
    ResourceUnit(resultYield, this.name)
  }

  override def toString = s"$name[$volume]"
}
case class Producible(name: String, recipe: Recipe, yieldTime: Long, since: Set[Prereq]) extends Resource {
  override def out(): ResourceUnit = ResourceUnit(defaultYield, this.name)
}

trait Cost {
  val cost: List[ResourceUnit]
}

case class Recipe(product: Producible, cost: List[ResourceUnit]) extends Cost {
  def price(amount: Int) = cost.map(y => ResourceUnit(y.value * amount, y.res))
}

case class ResourceUnit(value: Int, res: String)