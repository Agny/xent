package ru.agny.xent.core

import ru.agny.xent.core.ProductionQueue.ItemCount
import ru.agny.xent.core.Progress.ProductionTime

case class ProductionQueue(content: Vector[(DelayableItem, Int)], progress: ProductionTime = 0) {

  def in(item: DelayableItem, count: Int): ProductionQueue = {
    ProductionQueue((item, count) +: content, progress)
  }

  def out(fromTime: Long): (ProductionQueue, Vector[ItemCount]) = {
    val now = System.currentTimeMillis()
    val progress = now - (fromTime - this.progress)
    val (updatedContent, production, time) = handle(content, progress, Vector.empty)
    (ProductionQueue(updatedContent, time), production)
  }

  def isEmpty = content.isEmpty

  private def handle(items: Vector[ItemCount], remindedTime: Long, acc: Vector[ItemCount]): (Vector[ItemCount], Vector[ItemCount], ProductionTime) = {
    items match {
      case (res, amount) +: t => handle((res, amount), remindedTime, (res, 0)) match {
        case (_, time, (item, 0)) => (items, Vector.empty, time)
        case (0, time, prod) => handle(t, time, prod +: acc)
        case (count, time, prod) => ((res, count) +: t, prod +: acc, time)
      }
      case _ => (items, acc, 0)
    }
  }

  private def handle(item: ItemCount, remindedTime: Long, acc: ItemCount): (Int, ProductionTime, ItemCount) =
    (item, remindedTime) match {
      case ((_, 0), time) => (0, time, acc)
      case ((v, count), time) if time < v.yieldTime => (count, time, acc)
      case ((v, count), time) => handle((v, count - 1), time - v.yieldTime, (acc._1, acc._2 + 1))
    }
}

object ProductionQueue {
  type ItemCount = (DelayableItem, Int)

  def empty = ProductionQueue(Vector.empty)
}