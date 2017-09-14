package ru.agny.xent.core.city

import ru.agny.xent.core.utils.BuildingTemplate

import scala.collection.mutable

object ShapeProvider {
  private val shapes = mutable.Map[String, Shape]()

  def add(bt: BuildingTemplate) = {
    shapes += bt.name -> Shape.values(bt.shape)
    bt.shape
  }

  def delete(bName: String): Option[Shape] = shapes.remove(bName)

  def get(bName: String): Shape = shapes(bName)

}
