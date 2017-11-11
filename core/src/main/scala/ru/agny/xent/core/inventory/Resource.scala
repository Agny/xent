package ru.agny.xent.core.inventory

import ru.agny.xent.core.Prereq
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.utils.UserType.ItemWeight

sealed trait Resource extends DelayableItem {
  val since: Set[Prereq]
  val defaultYield = 1

  def out(): ItemStack

  override def toString = s"$name"
}

case class Extractable(id: ItemId, name: String, var volume: Int, yieldTime: ProgressTime, weight: ItemWeight, since: Set[Prereq]) extends Resource {
  override def out(): ItemStack = {
    val resultYield = if (volume > 0) defaultYield else 0
    volume = volume - resultYield
    ItemStack(resultYield, id, weight)
  }

  override def toString = s"$name[$volume]"
}
case class Obtainable(id: ItemId, name: String, yieldTime: ProgressTime, weight: ItemWeight, since: Set[Prereq]) extends Resource {
  override def out(): ItemStack = ItemStack(defaultYield, id, weight)
}
