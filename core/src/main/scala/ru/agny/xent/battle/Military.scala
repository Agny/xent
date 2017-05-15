package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.utils.SubTyper

case class Military(troops: Vector[(Troop, Occupation)]) {

  import ru.agny.xent.battle.OccupationSubTyper.implicits._

  def tick: (Military, Vector[Troop]) = {
    val troopsPositions = troops.foldLeft(Map.empty[Coordinate, Vector[(Troop, Occupation)]])(
      (positioned, t) => addPos(positioned, t._2.pos(t._1.moveSpeed, System.currentTimeMillis()), t)
    )
    val (alive, fallen) = troopsPositions.foldLeft(Vector.empty[(Troop, Occupation)], Vector.empty[Troop]) {
      (a, b) => val (inProcess, out) = findBattle(b._1, b._2)
        (inProcess ++ a._1, out ++ a._2)
    }
    (Military(alive), fallen)
  }

  private def addPos(poss: Map[Coordinate, Vector[(Troop, Occupation)]], pos: Coordinate, t: (Troop, Occupation)) =
    if (poss.contains(pos)) {
      val other = poss(pos)
      poss.updated(pos, t +: other)
    } else poss.updated(pos, Vector(t))

  private def findBattle(pos: Coordinate, inArea: Vector[(Troop, Occupation)]): (Vector[(Troop, Occupation)], Vector[Troop]) = inArea match {
    case multiple@h +: t if t.nonEmpty =>
      val (inBattle, queueing) = partition[Battle](multiple)
      val (battle, fallen) = commenceBattle(pos, inBattle, queueing).tick
      (multiple.map(x => (x._1, battle)), fallen)
    case single => (single, Vector.empty)
  }

  private def partition[By <: Occupation](troops: Vector[(Troop, Occupation)])(implicit ev: SubTyper[Occupation, By]) = {
    val l = Vector.empty[(Troop, By)]
    val r = Vector.empty[(Troop, Occupation)]
    troops.foldLeft(l, r)((lr, x) => (x._1, ev.asSub(x._2)) match {
      case (a, Some(o)) => ((a, o) +: lr._1, lr._2)
      case _ => (lr._1, x +: lr._2)
    })
  }

  private def commenceBattle(pos: Coordinate, inBattle: Vector[(Troop, Battle)], queueing: Vector[(Troop, Occupation)]): Battle = inBattle match {
    case (_, b) +: _ => b.addTroops(queueing.unzip._1.toVector)
    case _ => Battle(pos, queueing.unzip._1.toVector)
  }

}

object Military {
  val empty = Military(Vector.empty)
}
