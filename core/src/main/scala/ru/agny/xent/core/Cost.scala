package ru.agny.xent.core

import ru.agny.xent.ResourceUnit

trait Cost {
  val cost: Seq[ResourceUnit]
  def price(amount: Int) = cost.map(y => ResourceUnit(y.value * amount, y.res))
}

case class Recipe(product: Producible, cost: Seq[ResourceUnit]) extends Cost
