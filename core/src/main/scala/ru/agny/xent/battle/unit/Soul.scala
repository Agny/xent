package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.core._

case class Soul(id: ObjectId, level: LevelBar, spirit: SpiritBar, equip: Equipment, speed: Speed, skills: Seq[Skill]) {

  def defensePotential = Potential(equip.props(Defensive))

  def attackPotential = Potential(equip.props(Offensive))

  def attack(target: Troop): (Soul, Troop) = {
    val attackBy = attackPotential.strongestRaw
    val res = target.receiveDamage(Damage(attackBy.attr, attackBy.value))

    (this, res)
  }

  def receiveDamage(d: Damage) = {
    copy(spirit = spirit.change(defensePotential.to(d.by) - d.amount))
  }
}





