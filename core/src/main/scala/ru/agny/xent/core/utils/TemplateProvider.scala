package ru.agny.xent.core.utils

import scala.collection.mutable

object TemplateProvider {
  private val templates = mutable.Map[String, Vector[FacilityTemplate]]().withDefaultValue(Vector.empty)

  def add(layer: String, bt: FacilityTemplate) = {
    templates += layer -> (bt +: templates(layer))
    bt
  }

  def add(layer: String, bt: Vector[FacilityTemplate]) = {
    templates += layer -> (bt ++ templates(layer))
    bt
  }

  def get(layer: String, bName: String): Option[FacilityTemplate] = templates(layer).find(_.name == bName)

  def clear(layer: String) = templates += layer -> Vector.empty

}
