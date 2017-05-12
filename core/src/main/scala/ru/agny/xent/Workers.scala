package ru.agny.xent

import ru.agny.xent.UserType.ObjectId
import ru.agny.xent.battle.Occupation
import ru.agny.xent.battle.unit.Soul


// TODO have to handle soul transition from cell to cell
case class Workers(souls: Vector[(Soul, Occupation)]) {
  def callToArms(ids: Vector[ObjectId]): (Workers, Vector[Soul]) = {
    val (called, remains) = souls.partition {
      case (s, f) => !f.isBusy && ids.contains(s.id)
    }
    (Workers(remains), called.unzip._1)
  }
}

object Workers {
  val empty: Workers = Workers(Vector.empty)
}
