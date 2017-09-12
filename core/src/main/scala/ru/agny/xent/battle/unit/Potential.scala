package ru.agny.xent.battle.unit

import ru.agny.xent.core.unit.equip.{Attribute, AttrProperty}

/**
  * Represents effectiveness of equipment to resist/inflict damage with attribute
  */

case class Potential(stats: Vector[AttrProperty]) {

  def to(attribute: Attribute): Double = stats.find(x => x.prop == attribute) match {
    case Some(v) => v.value
    case None => 0
  }

  def weakestTo(against: Vector[AttrProperty]) = maxDiff(against, _ < _)

  def strongestRaw = stats.maxBy(_.value)

  def strongestTo(against: Vector[AttrProperty]) = maxDiff(against, _ > _)

  private def maxDiff(p: Vector[AttrProperty], f: (Int, Int) => Boolean) = p.foldLeft((p.head, 0))((max, attack) =>
    stats.find(x => x.prop == attack.prop) match {
      case Some(defense) => if (f(max._2, defense.value - attack.value)) (defense, defense.value - attack.value) else max
      case None => if (f(max._2, attack.value)) (attack, attack.value) else max
    }
  )
}

object Potential {
  val zero = Potential(Vector.empty)
}