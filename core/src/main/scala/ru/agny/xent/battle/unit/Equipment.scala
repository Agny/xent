package ru.agny.xent.battle.unit

import ru.agny.xent.battle.core.Equippable

case class Equipment(eq: Seq[Equippable])
object Equipment {
  def empty(): Equipment = Equipment(Seq.empty)
}
