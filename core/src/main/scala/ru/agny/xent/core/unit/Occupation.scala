package ru.agny.xent.core.unit

import ru.agny.xent.battle.Battle
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.unit.Speed.Distance
import ru.agny.xent.core.utils.SubTyper

trait Occupation {
  val isBusy = false

  def pos(): Coordinate
  def tick(distance: Distance): (Occupation, Distance)
}

object OccupationSubTyper {
  object implicits {
    implicit object BattleMatcher extends SubTyper[Occupation, Battle]
  }
}