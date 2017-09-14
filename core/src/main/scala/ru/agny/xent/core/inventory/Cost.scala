package ru.agny.xent.core.inventory

case class Cost(v: Vector[ItemStack]) {
  def price(amount: Int) = Cost(v.map(y => ItemStack(y.stackValue * amount, y.id)))
}