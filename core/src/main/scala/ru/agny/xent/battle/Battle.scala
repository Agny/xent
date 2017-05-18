package ru.agny.xent.battle

import ru.agny.xent.UserType.UserId
import ru.agny.xent.battle.unit.Speed.Speed
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.utils.TimeUnit

case class Battle(pos: Coordinate, troops: Map[Troop, Occupation], queue: Map[Troop, Occupation], start: ProgressTime, round: Round) extends Occupation {
  private type TO = (Troop, Occupation)

  def tick: (Option[Battle], Vector[TO]) = {
    val now = System.currentTimeMillis()
    val timeRemains = (start + round.time) - now
    if (timeRemains <= 0) toNextRound
    else (Some(this), Vector.empty)
  }

  def addTroops(t: Map[Troop, Occupation]): Battle = copy(queue = queue ++: t)

  private def toNextRound: (Option[Battle], Vector[TO]) = {
    ???
  }

  override val isBusy = true

  override def pos(speed: Speed, time: ProgressTime): Coordinate = pos
}

case class Round(n: Int, troops: Iterable[Troop]) {

  import Round._

  val time: ProgressTime = {
    val empty = Map.empty[UserId, Vector[Troop]].withDefaultValue(Vector.empty)
    val byUsers = troops.foldLeft(empty)((m, t) => m.updated(t.user, t +: m(t.user)))
    time(byUsers.map { case (uid, ts) => uid -> ts.foldLeft(0)((w, t) => w + weight(t)) }.values)
  }

  private def weight(t: Troop): Int = 15 //TODO weight calculation

  private def time(armiesWithWeight: Iterable[Int]): ProgressTime = {
    val max = armiesWithWeight.max
    val min = armiesWithWeight.min
    math.round((min.toDouble / max) * timeLimitMax)
  }
}

object Battle {
  def apply(pos: Coordinate, troops: Map[Troop, Occupation]): Battle = Battle(pos, troops, Map.empty, System.currentTimeMillis(), Round(1, troops.keys))
}

object Round {
  val timeLimitMax = TimeUnit.minute * 10
  val timeLimitMin = TimeUnit.minute
}
