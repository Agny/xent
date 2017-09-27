package ru.agny.xent.battle.unit

import ru.agny.xent.battle.Targetable
import ru.agny.xent.core.utils.UserType.{ObjectId, UserId}
import ru.agny.xent.core.unit.equip.{IncomeDamage, OutcomeDamage}

//TODO balance
case class Guard(id: ObjectId, spirit: Int, armor: Int) extends Targetable {
  override type Self = Guard

  override val weight = if (spirit > 0) spirit / 10 + armor * 2 else 0

  override def receiveDamage(d: OutcomeDamage) = {
    val damage = IncomeDamage(d.attr, Potential.zero, armor, d.calc())
    copy(spirit = spirit - damage.calc())
  }
}

object Guard {
  val speed = 10

  def tiered(tier: Int)(implicit user: UserId): Guard = tier match {
    case zero@0 => Guard(user, 20, 2)
    case first@1 => Guard(user, 40, 3)
    case second@2 => Guard(user, 100, 5)
    case third@3 => Guard(user, 200, 12)
    case last@4 => Guard(user, 400, 20)
  }
}
