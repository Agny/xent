package ru.agny.xent.battle.unit

import ru.agny.xent.battle.core._

case class Unit(level: LevelBar, spirit: SpiritBar, equip: Equipment, speed: Speed, skills: Seq[Skill]) {

  //  private val choosing = {u:Unit =>
  //    val sk = skills.collect{case s:Offensive => s}
  //    u.equip.eq.foldLeft(u.equip.eq.head)((a,b) => {
  //      a.a
  //    })
  //  }
  //
  val defensePotential: Seq[Property] = equip.props(Defensive)
  val attackPotential: Seq[Property] = equip.props(Offensive)

  def attack(target: Troop): (Unit, Troop) = {
    ??? //TODO
  }

  private def selectTargets(enemy: Troop): Seq[Unit] = {
    ???
    //    enemy.units.foldLeft(enemy.units.head)((a,b) => )
  }
}






