package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Speed.Speed
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.utils.SubTyper

trait Occupation {
  val isBusy = false
  val start: ProgressTime

  def pos(speed: Speed, time: ProgressTime): Coordinate
}

object OccupationSubTyper {
  object implicits {
    implicit object BattleMatcher extends SubTyper[Occupation, Battle] {
      override def asSub(a: Occupation): Option[Battle] = a match {
        case a: Battle => Some(a)
        case _ => None
      }
    }
  }
}