package ru.agny.xent.core

trait Cost {
  val cost: List[ResourceUnit]
  def price(amount: Int) = cost.map(y => ResourceUnit(y.value * amount, y.res))
}

case class Recipe(product: Producible, cost: List[ResourceUnit]) extends Cost
case class ResourceUnit(value: Int, res: String)
