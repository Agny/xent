package ru.agny.xent.core

import ru.agny.xent.core.Progress._
import ru.agny.xent.core.ResourceQueue.ItemCount

trait ResourceQueue {
  def in(item: DelayableItem, count: Int): ResourceQueue

  def out(period: ProgressTime): (ResourceQueue, Vector[ItemCount])

  def isEmpty: Boolean

  val progress: ProgressTime
}

object ResourceQueue {
  type ItemCount = (DelayableItem, Int)
}
