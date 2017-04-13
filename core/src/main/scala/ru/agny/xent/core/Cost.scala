package ru.agny.xent.core

trait Cost {
  val cost: Vector[ResourceUnit]
  def price(amount: Int) = cost.map(y => ResourceUnit(y.stackValue * amount, y.id))
}

case class Recipe(product: Producible, cost: Vector[ResourceUnit]) extends Cost
