package ru.agny.xent.core

import ru.agny.xent.Response
import scala.collection.immutable.Queue

trait Facility {
  val name: String
  val resources: List[Resource]
  protected var progress: ProductionProgressTime = 0
  //TODO common progress for queued and simple items
  protected var queue = ProductionQueue(Queue.empty)
  type ProductionProgressTime = Long

  def tick(fromTime: Long): Storage => Storage = storage => {
    if (queue.isEmpty) {
      resources.collect { case x: Simple => x }.foldRight(storage)((res,s) =>
        s.add(extract(res, System.currentTimeMillis() - fromTime + progress, ResourceUnit(0, res.name)))
      )
    } else {
      val (updatedQueue, production) = queue.out(fromTime)
      queue = updatedQueue
      storage.add(production)
    }
  }

  def addToQueue(item: (Recipe, Int)): Storage => Either[Response, (Recipe, Int)] = storage => {
    storage.spend(Recipe(item._1.product, item._1.price(item._2))) match {
      case Left(s) => Left(s)
      case Right(s) => queue = queue.in(item._1, item._2); Right(item)
    }
  }

  protected def extract(res: Resource, reminded: ProductionProgressTime, extracted: ResourceUnit): ResourceUnit = {
    res match {
      case x: Finite if x.volume == 0 => extracted
      case x => extract_rec(res, reminded, extracted)
    }
  }

  private def extract_rec(res: Resource, reminded: ProductionProgressTime, extracted: ResourceUnit): ResourceUnit = {
    if (reminded < res.yieldTime) {
      progress = reminded
      extracted
    } else {
      extract_rec(res, reminded - res.yieldTime, ResourceUnit(extracted.value + res.out().value, extracted.res))
    }
  }
}

case class Building(name: String, resources: List[Resource]) extends Facility
case class Outpost(name: String, main: Extractable, resources: List[Resource]) extends Facility {

  override def tick(fromTime: ProductionProgressTime): (Storage) => Storage = storage => {
    if (queue.isEmpty) {
      storage.add(extract(main,  System.currentTimeMillis() - fromTime + progress, ResourceUnit(0, main.name)))
    } else {
      val (updatedQueue, production) = queue.out(fromTime)
      queue = updatedQueue
      storage.add(production)
    }
  }
}

case class ProductionQueue(private val content: Seq[(Recipe, Int)]) {

  def in(item: Recipe, count: Int): ProductionQueue = {
    ProductionQueue(content :+(item, count))
  }

  def out(fromTime: Long): (ProductionQueue, List[ResourceUnit]) = {
    val now = System.currentTimeMillis()
    val progress = now - fromTime
    val (updatedContent, production) = handle(content, progress, List.empty)
    (ProductionQueue(updatedContent), production)
  }

  def isEmpty = content.isEmpty

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