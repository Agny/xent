package ru.agny.xent.battle.unit

case class Troop(units: Seq[Unit]) {

  def attack(other: Troop): (Troop, Troop) = {
    val (u, t) = units.foldLeft((Seq.empty[Unit], other))(handleBattle)
    (Troop(u), t)
  }

  private def handleBattle(state: (Seq[Unit], Troop), attacker: Unit): (Seq[Unit], Troop) = {
    val (unitState, newTroopState) = attacker.attack(state._2)
    (state._1 :+ unitState, newTroopState)
  }

}
