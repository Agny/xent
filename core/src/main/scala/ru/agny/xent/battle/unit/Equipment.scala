package ru.agny.xent.battle.unit

import ru.agny.xent.battle.core.Mode
import ru.agny.xent.battle.core.{Property, Attribute, Equippable}

case class Equipment(private val set: Seq[Equippable]) {

  def props(implicit mode: Mode): Seq[Property] =
    set.foldLeft(Map.empty[Attribute, Int])((a, b) =>
      b.attrs.foldLeft(a)(collectPotential)
    ).map(x => Property(x._1, x._2, mode)).toSeq

  private def collectPotential(attrs: Map[Attribute, Int], prop: Property)(implicit mode: Mode) = prop.mode match {
    case correct if correct == mode =>
      if (attrs.contains(prop.attr)) {
        attrs + (prop.attr -> (attrs(prop.attr) + prop.value))
      } else {
        attrs + (prop.attr -> prop.value)
      }
    case _ => attrs
  }
}
object Equipment {
  def empty(): Equipment = Equipment(Seq.empty)
}
