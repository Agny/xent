package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Progress._
import ru.agny.xent.core.utils.{NESeq, TimeUnit}

case class Round(n: Int, troops: NESeq[Troop], story: ProgressTime = 0) {

  import Round._

  val duration: ProgressTime = {
    val byUsers = Troop.groupByUsers(troops)
    time(byUsers.map { case (uid, ts) => uid -> ts.foldLeft(0)((w, t) => w + t.weight) }.values)
  }

  private def time(armiesByUserWithWeight: Iterable[Int]): ProgressTime = {
    if (armiesByUserWithWeight.size < 2) 0
    else {
      val max = armiesByUserWithWeight.max
      val min = armiesByUserWithWeight.min
      val time = math.round((min.toDouble / max) * timeLimitMax)
      if (time < timeLimitMin) timeLimitMin else time
    }
  }
}

object Round {
  val timeLimitMax = TimeUnit.minute * 10
  val timeLimitMin = TimeUnit.minute

  def apply(prev: Round, troops: NESeq[Troop]): Round = Round(prev.n + 1, troops, prev.story + prev.duration)
}
