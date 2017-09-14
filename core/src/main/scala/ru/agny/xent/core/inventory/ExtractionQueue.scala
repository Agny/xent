package ru.agny.xent.core.inventory

import ru.agny.xent.core.inventory.Progress._
import ru.agny.xent.core.inventory.ResourceQueue.ItemCount

import scala.annotation.tailrec

case class ExtractionQueue(content: Extractable, progress: ProgressTime = 0) extends ResourceQueue {
  override def in(item: DelayableItem, count: Int): ExtractionQueue = this

  override def out(period: ProgressTime): (ExtractionQueue, Vector[(DelayableItem, Int)]) = {
    val (production, time) = handle(content, progress + period, 0)
    (copy(progress = time), Vector(production))
  }

  @tailrec private def handle(source: Extractable, remindedTime: Long, count: Int): (ItemCount, ProgressTime) =
    (source, remindedTime) match {
      case (exhausted, _) if exhausted.volume == 0 => ((exhausted, count), 0)
      case (r, notEnoughTime) if notEnoughTime < r.yieldTime => ((r, count), notEnoughTime)
      case (r, t) => handle(r, t - r.yieldTime, count + r.out().stackValue)
    }

  override def isEmpty: Boolean = content.volume <= 0
}