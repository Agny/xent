package ru.agny.xent.core

import ru.agny.xent.core.Progress._
import ru.agny.xent.core.ResourceQueue.ItemCount

trait ResourceQueue {
  def in(item: DelayableItem, count: Int): ResourceQueue

  def out(from: ProgressTime): (ResourceQueue, Vector[ItemCount])
}

object ResourceQueue {
  type ItemCount = (DelayableItem, Int)
}
