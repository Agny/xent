package ru.agny.xent.core.city

import ru.agny.xent.core.utils.UserType.ObjectId
import ru.agny.xent.battle.Waiting
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.unit.{Occupation, Soul}

// TODO have to handle soul transition from cell to cell
case class Workers(souls: Vector[(Soul, Occupation)]) {
  def callToArms(ids: Vector[ObjectId]): (Workers, Vector[Soul]) = {
    val (called, remains) = souls.partition {
      case (s, f) => !f.isBusy && ids.contains(s.id)
    }
    (Workers(remains), called.unzip._1)
  }

  def addNew(soul: Soul, cityPos: Coordinate): Workers = Workers((soul, new Waiting(cityPos)) +: souls)
}

object Workers {
  val empty: Workers = Workers(Vector.empty)
}
