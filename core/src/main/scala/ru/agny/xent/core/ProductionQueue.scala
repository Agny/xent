package ru.agny.xent.core

case class ProductionQueue(content: Seq[(DelayableItem, Int)]) {

  def in(item: DelayableItem, count: Int): ProductionQueue = {
    ProductionQueue(content :+(item, count))
  }

  def out(fromTime: Long): (ProductionQueue, Seq[(DelayableItem, Int)]) = {
    val now = System.currentTimeMillis()
    val progress = now - fromTime
    val (updatedContent, production) = handle(content, progress, Seq.empty)
    (ProductionQueue(updatedContent), production)
  }

  def isEmpty = content.isEmpty

  private def handle(items: Seq[(DelayableItem, Int)], remindedTime: Long, acc: Seq[(DelayableItem, Int)]): (Seq[(DelayableItem, Int)], Seq[(DelayableItem, Int)]) = {
    items match {
      case Seq(h, t@_*) => handle(h, remindedTime, (h._1, 0)) match {
        case (_, time, (item, 0)) => (items, Seq.empty)
        case (0, time, prod) => handle(t, time, prod +: acc)
        case (count, time, prod) => ((h._1, count) +: t, prod +: acc)
      }
      case _ => (items, acc)
    }
  }

  private def handle(item: (DelayableItem, Int), remindedTime: Long, acc: (DelayableItem, Int)): (Int, Progress.ProductionTime, (DelayableItem, Int)) =
    (item, remindedTime) match {
      case ((_, 0), time) => (0, time, acc)
      case ((v, count), time) if time < v.yieldTime => (count, time, acc)
      case ((v, count), time) => handle((v, count - 1), time - v.yieldTime, (acc._1, acc._2 + 1))
    }
}

object ProductionQueue {
  def empty() = ProductionQueue(Seq.empty)
}