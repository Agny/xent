package ru.agny.xent.war

import ru.agny.xent.unit.Soul

case class Defence(units: Seq[Soul]) {
  def isEliminated(): Boolean = !units.exists(_.state() == Soul.State.Active)
}
object Defence {
  val Empty = Defence(Seq.empty)
}
