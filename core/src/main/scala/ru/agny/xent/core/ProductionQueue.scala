package ru.agny.xent.core

import ru.agny.xent.core.Progress.ProductionTime

case class ProductionQueue(content: Vector[(DelayableItem, Int)], progress: ProductionTime = 0) {

  def in(item: DelayableItem, count: Int): ProductionQueue = {
    ProductionQueue((item, count) +: content, progress)
  }

  def out(fromTime: Long): (ProductionQueue, Vector[(DelayableItem, Int)]) = {
    val now = System.currentTimeMillis()
    val progress = now - (fromTime - this.progress)
    val (updatedContent, production, time) = handle(content, progress, Vector.empty)
    (ProductionQueue(updatedContent, time), production)
  }

  def isEmpty = content.isEmpty

  private def handle(items: Vector[(DelayableItem, Int)], remindedTime: Long, acc: Vector[(DelayableItem, Int)]): (Vector[(DelayableItem, Int)], Vector[(DelayableItem, Int)], ProductionTime) = {
    items match {
      case h +: t => handle(h, remindedTime, (h._1, 0)) match {
        case (_, time, (item, 0)) => (items, Vector.empty, time)
        case (0, time, prod) => handle(t, time, prod +: acc)
        case (count, time, prod) => ((h._1, count) +: t, prod +: acc, time)
      }
      case _ => (items, acc, 0)
    }
  }

  private def handle(item: (DelayableItem, Int), remindedTime: Long, acc: (DelayableItem, Int)): (Int, ProductionTime, (DelayableItem, Int)) =
    (item, remindedTime) match {
      case ((_, 0), time) => (0, time, acc)
      case ((v, count), time) if time < v.yieldTime => (count, time, acc)
      case ((v, count), time) => handle((v, count - 1), time - v.yieldTime, (acc._1, acc._2 + 1))
    }
}

object ProductionQueue {
  def empty = ProductionQueue(Vector.empty)
}