package ru.agny.xent.trade

import ru.agny.xent.core.inventory.ItemStack

case class Price(amount: ItemStack) extends Ordered[Price] {
  override def compare(that: Price) = amount.stackValue - that.amount.stackValue
}
