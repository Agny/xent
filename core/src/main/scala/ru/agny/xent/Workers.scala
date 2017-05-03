package ru.agny.xent

import ru.agny.xent.battle.unit.Soul
import ru.agny.xent.core.Facility

// TODO have to handle soul transition from cell to cell
case class Workers(souls: Vector[(Soul, Facility)])

object Workers {
  def empty: Workers = Workers(Vector.empty)
}
