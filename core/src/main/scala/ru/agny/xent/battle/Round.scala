package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.MapObject
import ru.agny.xent.core.Progress._
import ru.agny.xent.core.utils.{NESeq, TimeUnit}

case class Round(n: Int, troops: NESeq[MapObject], progress: ProgressTime = 0) {

  import Round._

  val duration: ProgressTime = {
    val byUsers = Troop.groupByUsers(troops)
    time(byUsers.map { case (uid, ts) => uid -> ts.foldLeft(0)((w, t) => w + t.weight) }.values)
  }

  def tick(t: ProgressTime): Round = copy(progress = progress + t)

  def next(t: NESeq[MapObject]): Round = Round(n + 1, t)

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
  val timeLimitMax: Long = TimeUnit.minute * 10
  val timeLimitMin: Long = TimeUnit.minute
}
