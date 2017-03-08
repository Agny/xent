package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.core._
import ru.agny.xent.battle.unit.inventory.Equipment

case class Soul(id: ObjectId, level: LevelBar, spirit: SpiritBar, equip: Equipment, speed: Int, skills: Seq[Skill]) {

  def defensePotential = Potential(equip.props(Defensive))

  def attackPotential = Potential(equip.props(Offensive))

  def attack(target: Troop): (Soul, Troop) = Tactic.get(this).execute(target)

  def receiveDamage(d: Damage) = {
    copy(spirit = spirit.change(defensePotential.to(d.by) - d.amount))
  }
}





