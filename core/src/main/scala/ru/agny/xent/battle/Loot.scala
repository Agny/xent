package ru.agny.xent.battle

import ru.agny.xent.core.inventory.ItemStack

trait Loot {
  def get: Vector[ItemStack]
}

object Loot {
  def apply(items: Vector[ItemStack]): Loot = InnerLoot(items)
  private case class InnerLoot(items: Vector[ItemStack]) extends Loot {
    override def get = items
  }
}
