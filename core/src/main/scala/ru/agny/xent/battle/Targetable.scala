package ru.agny.xent.battle

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.core.unit.equip.OutcomeDamage

trait Targetable {
  type Self <: Targetable
  val id: ObjectId
  val spirit: Int
  val weight: Int

  def receiveDamage(d: OutcomeDamage): Self
}
