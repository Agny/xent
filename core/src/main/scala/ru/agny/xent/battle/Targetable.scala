package ru.agny.xent.battle

import ru.agny.xent.core.utils.UserType.ObjectId
import ru.agny.xent.core.unit.equip.OutcomeDamage
import ru.agny.xent.core.utils.SelfAware

trait Targetable extends SelfAware {
  val id: ObjectId
  val spirit: Int
  val weight: Int

  def receiveDamage(d: OutcomeDamage): Self
}
