package ru.agny.xent.core.inventory

import ru.agny.xent.core.inventory.Progress._
import ru.agny.xent.core.inventory.ResourceQueue.ItemCount

trait ResourceQueue {
  def in(item: DelayableItem, count: Int): ResourceQueue

  def out(period: ProgressTime): (ResourceQueue, Vector[ItemCount])

  def isEmpty: Boolean

  val progress: ProgressTime
}

object ResourceQueue {
  type ItemCount = (DelayableItem, Int)
}
