package ru.agny.xent.core

trait Cost {
  val cost: Seq[ResourceUnit]
  def price(amount: Int) = cost.map(y => ResourceUnit(y.stackValue * amount, y.id))
}

case class Recipe(product: Producible, cost: Seq[ResourceUnit]) extends Cost
