package ru.agny.xent.battle.unit

import ru.agny.xent.battle.core.{Attribute, Property}

/**
  * Describe effectiveness of equipment to resist/inflict damage with attribute
  */

case class Potential(stats: Vector[Property]) {

  def to(attribute: Attribute): Double = stats.find(x => x.attr == attribute) match {
    case Some(v) => v.value
    case None => 0
  }

  def weakestTo(against: Vector[Property]) = maxDiff(against, _ < _)

  def strongestRaw = stats.maxBy(_.value)

  def strongestTo(against: Vector[Property]) = maxDiff(against, _ > _)

  private def maxDiff(p: Vector[Property], f: (Int, Int) => Boolean) = p.foldLeft((p.head, 0))((max, attack) =>
    stats.find(x => x.attr == attack.attr) match {
      case Some(defense) => if (f(max._2, defense.value - attack.value)) (defense, defense.value - attack.value) else max
      case None => if (f(max._2, attack.value)) (attack, attack.value) else max
    }
  )
}