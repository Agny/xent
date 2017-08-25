package ru.agny.xent.core.unit

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.Tactic
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.unit.equip._

case class Soul(id: ObjectId, stats: SoulData, private val equip: Equipment) extends Levelable {

  private implicit val eq = equip

  lazy val weight = stats.weight
  lazy val endurance = stats.endurance
  lazy val initiative = stats.initiative
  lazy val speed = stats.speed
  lazy val spirit = stats.spirit
  lazy val state = spirit.points match {
    case alive if alive > 0 => Soul.Active
    case _ => Soul.Fallen
  }

  def attackRates(implicit target: Troop) = stats.attackModifiers

  def attack(target: Troop): (Soul, Troop) = Tactic.get(this).execute(target)

  def receiveDamage(d: OutcomeDamage) = {
    val damage = IncomeDamage(d.attr, stats.defenseModifiers, stats.armor, d.calc())
    copy(stats = stats.receiveDamage(damage.calc()))
  }

  def lose(): (Soul, Equipment) = (copy(equip = Equipment.empty), equip)
}

object Soul {
  trait State
  case object Active extends State
  case object Fallen extends State
}





