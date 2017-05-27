package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Progress._
import ru.agny.xent.core.utils.{NESeq, TimeUnit}

case class Round(n: Int, troops: NESeq[Troop]) {

  import Round._

  val duration: ProgressTime = {
    val byUsers = Troop.groupByUsers(troops)
    time(byUsers.map { case (uid, ts) => uid -> ts.foldLeft(0)((w, t) => w + weight(t)) }.values)
  }

  private def weight(t: Troop): Int = troops.foldLeft(0)((sum, x) => sum + x.weight)

  private def time(armiesByUserWithWeight: Iterable[Int]): ProgressTime = {
    if (armiesByUserWithWeight.size < 2) 0
    else {
      val max = armiesByUserWithWeight.max
      val min = armiesByUserWithWeight.min
      math.round((min.toDouble / max) * timeLimitMax)
    }
  }
}

object Round {
  val timeLimitMax = TimeUnit.minute * 10
  val timeLimitMin = TimeUnit.minute
}
