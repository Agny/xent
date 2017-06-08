package ru.agny.xent.core

import ru.agny.xent.core.Progress._
import ru.agny.xent.core.ResourceQueue._

import scala.annotation.tailrec

case class ExtractionQueue(content: Extractable, progress: ProgressTime = 0) extends ResourceQueue {
  override def in(item: DelayableItem, count: Int): ExtractionQueue = this

  override def out(from: ProgressTime): (ExtractionQueue, Vector[(DelayableItem, Int)]) = {
    val now = System.currentTimeMillis()
    val progress = now - (from - this.progress)
    val (production, time) = handle(content, progress, 0)
    (copy(progress = time), Vector(production))
  }

  @tailrec private def handle(source: Extractable, remindedTime: Long, count: Int): (ItemCount, ProgressTime) =
    (source, remindedTime) match {
      case (exhausted, _) if exhausted.volume == 0 => ((exhausted, count), 0)
      case (r, notEnoughTime) if notEnoughTime < r.yieldTime => ((r, count), notEnoughTime)
      case (r, t) => handle(r, t - r.yieldTime, count + r.out().stackValue)
    }
}