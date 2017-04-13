package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.core._
import ru.agny.xent.battle.unit.inventory.{Weapon, Equipment}

case class Soul(id: ObjectId, level: LevelBar, spirit: SpiritBar, equip: Equipment, speed: Int, skills: Vector[Skill]) {

  def defensePotential = Potential(equip.props()(Defensive))

  def attackPotential(wpn: Weapon) = Potential(equip.props(wpn)(Offensive))

  def attack(target: Troop): (Soul, Troop) = Tactic.get(this).execute(target)

  def receiveDamage(d: OutcomeDamage) = {
    val damage = IncomeDamage(d.attr, defensePotential, equip.armor.value, d.calc())
    copy(spirit = spirit.change(-damage.calc()))
  }
}





