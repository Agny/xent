package ru.agny.xent.battle.unit

case class Troop(units: Seq[Unit]) {

  def attack(other: Troop): (Troop, Troop) = {
    units.foldLeft(Seq.empty[Unit])((a,b) =>  a:+ b.attack(other)._1 )
  }

}
