package ru.agny.xent.core

import ru.agny.xent.core.utils.BuildingTemplate

import scala.collection.mutable

object ShapeProvider {
  private val shapes = mutable.Map[String, Shape]()

  def add(bt: BuildingTemplate) = {
    shapes += bt.name -> bt.shape
    bt.shape
  }

  def get(bName: String): Shape = shapes(bName)

}
