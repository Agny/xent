package ru.agny.xent.core

import Item.ItemId
import ru.agny.xent.core.Progress.ProgressTime

sealed trait Resource extends DelayableItem {
  val since: Set[Prereq]
  val defaultYield = 1

  def out(): ItemStack

  override def toString = s"$name"
}

case class Extractable(id: ItemId, name: String, var volume: Int, yieldTime: ProgressTime, since: Set[Prereq]) extends Resource {
  override def out(): ItemStack = {
    val resultYield = if (volume > 0) defaultYield else 0
    volume = volume - resultYield
    ItemStack(resultYield, id)
  }

  override def toString = s"$name[$volume]"
}
case class Obtainable(id: ItemId, name: String, yieldTime: ProgressTime, since: Set[Prereq]) extends Resource {
  override def out(): ItemStack = ItemStack(defaultYield, id)
}
