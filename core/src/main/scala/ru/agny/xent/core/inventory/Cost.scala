package ru.agny.xent.core.inventory

case class Cost(v: Vector[Item]) {
  def price(amount: Int) = Cost(v.foldLeft(Vector.empty[Item]) {
    case (acc, x: ItemStack) => ItemStack(x.stackValue * amount, x.id) +: acc
    case (acc, item) => (0 until amount map (_ => item)).toVector ++ acc
  })
}

object Cost {
  def apply(v: ItemStack): Cost = Cost(Vector(v))
}
