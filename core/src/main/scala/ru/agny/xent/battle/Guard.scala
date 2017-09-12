package ru.agny.xent.battle

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.unit.Potential
import ru.agny.xent.core.unit.equip.{IncomeDamage, OutcomeDamage}

//TODO balance
case class Guard(id: ObjectId, spirit: Int, armor: Int) extends Targetable {
  override type Self = Guard

  override val weight = spirit / 10 + armor * 2

  override def receiveDamage(d: OutcomeDamage) = {
    val damage = IncomeDamage(d.attr, Potential.zero, armor, d.calc())
    copy(spirit = spirit - damage.calc())
  }
}

object Guard {
  val speed = 10
}
