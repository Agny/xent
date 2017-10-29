package ru.agny.xent.battle

import ru.agny.xent.core.inventory.Item

trait Loot {
  def get: Vector[Item]
}

object Loot {
  def apply(items: Vector[Item]): Loot = InnerLoot(items)
  private case class InnerLoot(items: Vector[Item]) extends Loot {
    override def get = items
  }
}
