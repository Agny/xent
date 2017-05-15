package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Speed.Speed
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime

case class Battle(pos: Coordinate, troops: Vector[Troop], queue: Vector[Troop], start: ProgressTime) extends Occupation {
  override val isBusy = true

  override def pos(speed: Speed, time: ProgressTime): Coordinate = pos

  def tick: (Battle, Vector[Troop]) = ???

  def addTroops(t: Vector[Troop]): Battle = copy(queue = queue ++: t)
}

object Battle {
  def apply(pos: Coordinate, troops: Vector[Troop]): Battle = Battle(pos, troops, Vector.empty, System.currentTimeMillis())
}
