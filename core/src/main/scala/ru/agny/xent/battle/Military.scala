package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.utils.SubTyper

case class Military(troops: Vector[(Troop, Occupation)]) {

  import ru.agny.xent.battle.OccupationSubTyper.implicits._

  def tick: Military = {
    val troopsPositions = troops.foldLeft(Map.empty[Coordinate, List[(Troop, Occupation)]])((positioned, t) =>
      addPos(positioned, t._2.pos(t._1.moveSpeed, System.currentTimeMillis()), t))
    val r = troopsPositions.flatMap(x => findBattle(x._1, x._2))
    Military(r.toVector)
  }

  private def addPos(poss: Map[Coordinate, List[(Troop, Occupation)]], pos: Coordinate, t: (Troop, Occupation)) =
    if (poss.contains(pos)) {
      val other = poss(pos)
      poss.updated(pos, t +: other)
    } else poss.updated(pos, List(t))

  private def findBattle(pos: Coordinate, inArea: List[(Troop, Occupation)]): List[(Troop, Occupation)] = inArea match {
    case multiple@h :: t if t.nonEmpty =>
      val (inBattle, queueing) = partition[Battle](multiple)
      val battle = commenceBattle(pos, inBattle, queueing)
      multiple.map(x => (x._1, battle.tick))
    case single => single
  }

  private def partition[By <: Occupation](troops: List[(Troop, Occupation)])(implicit ev: SubTyper[Occupation, By]) = {
    val l = List.empty[(Troop, By)]
    val r = List.empty[(Troop, Occupation)]
    troops.foldLeft(l, r)((lr, x) => (x._1, ev.asSub(x._2)) match {
      case (a, Some(o)) => ((a, o) +: lr._1, lr._2)
      case _ => (lr._1, x +: lr._2)
    })
  }

  private def commenceBattle(pos: Coordinate, inBattle: List[(Troop, Battle)], queueing: List[(Troop, Occupation)]): Battle = inBattle match {
    case (_, b) :: _ => b.addTroops(queueing.unzip._1.toVector)
    case Nil => Battle(pos, queueing.unzip._1.toVector)
  }

}

object Military {
  val empty = Military(Vector.empty)
}
