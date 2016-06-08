package ru.agny.xent.core

import ru.agny.xent.Error
import scala.collection.immutable.Queue

sealed trait Facility {
  val id: Int
  val name: String
  val cost: List[ResourceUnit]
  var progress: ProductionProgressTime = 0
  type ProductionProgressTime = Long

  def tick(fromTime: Long): Storage => Storage
}
case class Outpost(id: Int, name: String, resource: Extractable, cost: List[ResourceUnit]) extends Facility {
  override def tick(fromTime: Long): Storage => Storage = {
    storage => storage.add(extract(System.currentTimeMillis() - fromTime + progress, ResourceUnit(0, resource.name)))
  }

  private def extract(remindedTime: Long, extracted: ResourceUnit): ResourceUnit = {
    if(resource.volume == 0) extracted
    else if (remindedTime < resource.yieldTime) {
      progress = remindedTime
      extracted
    } else {
      extract(remindedTime - resource.yieldTime, ResourceUnit(extracted.value + resource.out().value, extracted.res))
    }
  }

}
case class Building(id: Int, name: String, resources: List[Producible], cost: List[ResourceUnit]) extends Facility {
  private var queue = ProductionQueue(Queue.empty)

  override def tick(fromTime: Long): Storage => Storage = storage => {
    val (updatedQueue, production) = queue.out(fromTime)
    queue = updatedQueue
    storage.add(production)
  }

  def addToQueue(item: (Recipe, Int)): Storage => Either[Error, (Recipe, Int)] = storage => {
    storage.spend(Recipe(item._1.product, item._1.price(item._2))) match {
      case Left(s) => Left(s)
      case Right(s) => queue = queue.in(item._1, item._2); Right(item)
    }
  }
}

case class ProductionQueue(content: Seq[(Recipe, Int)]) {

  def in(item: Recipe, count: Int): ProductionQueue = {
    ProductionQueue(content :+(item, count))
  }

  def out(fromTime: Long): (ProductionQueue, List[ResourceUnit]) = {
    val now = System.currentTimeMillis()
    val progress = now - fromTime
    val (updatedContent, production) = handle(content, progress, List.empty)
    (ProductionQueue(updatedContent), production)
  }

  private def handle(items: Seq[(Recipe, Int)], remindedTime: Long, production: List[ResourceUnit]): (Seq[(Recipe, Int)], List[ResourceUnit]) = {
    items match {
      case x :: xs => handle(x, remindedTime, ResourceUnit(0, x._1.product.name)) match {
        case (0, time, prod) => handle(items, time, prod :: production)
        case (count, time, prod) => ((x._1, count) :: xs, prod :: production)
      }
      case _ => (items, production)
    }
  }

  private def handle(item: (Recipe, Int), remindedTime: Long, production: ResourceUnit): (Int, Long, ResourceUnit) =
    (item, remindedTime) match {
      case ((_, 0), time) => (0, time, production)
      case ((v, count), time) if time < v.product.yieldTime => (count, time, production)
      case ((v, count), time) => handle((v, count - 1), time - v.product.yieldTime, ResourceUnit(production.value + 1, v.product.name))
    }
}