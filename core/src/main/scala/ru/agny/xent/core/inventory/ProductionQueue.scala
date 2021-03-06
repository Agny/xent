package ru.agny.xent.core.inventory

import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.inventory.ResourceQueue.ItemCount

import scala.annotation.tailrec

case class ProductionQueue(content: Vector[(DelayableItem, Int)], progress: ProgressTime = 0) extends ResourceQueue {

  override def in(item: DelayableItem, count: Int): ProductionQueue = {
    ProductionQueue((item, count) +: content, progress)
  }

  override def out(period: ProgressTime): (ProductionQueue, Vector[ItemCount]) = {
    val (updatedContent, production, time) = handle(content, progress + period, Vector.empty)
    (ProductionQueue(updatedContent, time), production)
  }

  override def isEmpty: Boolean = content.isEmpty

  @tailrec private def handle(items: Vector[ItemCount], remindedTime: Long, acc: Vector[ItemCount]): (Vector[ItemCount], Vector[ItemCount], ProgressTime) = {
    items match {
      case (res, amount) +: t => handle((res, amount), remindedTime, (res, 0)) match {
        case (_, time, (item, 0)) => (items, Vector.empty, time)
        case (0, time, prod) => handle(t, time, prod +: acc)
        case (count, time, prod) => ((res, count) +: t, prod +: acc, time)
      }
      case _ => (items, acc, 0)
    }
  }

  @tailrec private def handle(item: ItemCount, remindedTime: Long, acc: ItemCount): (Int, ProgressTime, ItemCount) =
    (item, remindedTime) match {
      case ((_, 0), time) => (0, time, acc)
      case ((v, count), time) if time < v.yieldTime => (count, time, acc)
      case ((v, count), time) => handle((v, count - 1), time - v.yieldTime, (acc._1, acc._2 + 1))
    }
}

object ProductionQueue {
  val empty = ProductionQueue(Vector.empty)
}