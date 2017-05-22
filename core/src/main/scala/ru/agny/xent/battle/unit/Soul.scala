package ru.agny.xent.battle.unit

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.core._
import ru.agny.xent.battle.unit.inventory.{Weapon, Equipment}

case class Soul(id: ObjectId, level: LevelBar, spirit: SpiritBar, equip: Equipment, speed: Int, skills: Vector[Skill]) {
  //TODO should depends on stats
  lazy val weight = 10
  lazy val endurance = 3
  lazy val initiative = 5
  lazy val state = spirit.points match {
    case alive if alive > 0 => Soul.Active
    case _ => Soul.Fallen
  }

  def defensePotential = Potential(equip.props()(Defensive))

  def attackPotential(wpn: Weapon) = Potential(equip.props(wpn)(Offensive))

  def attack(target: Troop): (Soul, Troop) = Tactic.get(this).execute(target)

  def receiveDamage(d: OutcomeDamage) = {
    val damage = IncomeDamage(d.attr, defensePotential, equip.armor.value, d.calc())
    copy(spirit = spirit.change(-damage.calc()))
  }

  def lose(): (Soul, Equipment) = (copy(equip = Equipment.empty), equip)
}

object Soul {
  trait State
  case object Active extends State
  case object Fallen extends State
}





