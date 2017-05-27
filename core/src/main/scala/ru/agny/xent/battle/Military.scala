package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.utils.{NESeq, SubTyper}

case class Military(troops: Vector[(Troop, Occupation)]) {

  import ru.agny.xent.battle.Military._

  def tick: (Military, Vector[Troop]) = {
    val empty = Map.empty[Coordinate, Vector[TO]].withDefaultValue(Vector.empty)
    val troopsPositions = troops.foldLeft(empty) { (positioned, t) =>
      val pos = t._2.pos(t._1.moveSpeed, System.currentTimeMillis())
      addPos(positioned, pos, t)
    }
    val (alive, toErase) = troopsPositions.foldLeft(Vector.empty[TO], Vector.empty[Troop]) { (a, b) =>
      val (inBattle, out) = findBattle(b._1, b._2)
      val (moving, fallenArrived) = arrive(out)
      (moving ++ inBattle ++ a._1, fallenArrived ++ a._2)
    }
    (Military(alive), toErase)
  }
}

object Military {

  import ru.agny.xent.battle.OccupationSubTyper.implicits._

  private type TO = (Troop, Occupation)
  private type TB = (Troop, Battle)

  val empty = Military(Vector.empty)

  private def addPos(ct: Map[Coordinate, Vector[TO]], pos: Coordinate, t: TO) = ct.updated(pos, t +: ct(pos))

  //TODO fallen troop should ignore fights
  private def findBattle(pos: Coordinate, inArea: Vector[TO]): (Iterable[TB], Iterable[TO]) = inArea match {
    case multiple@h +: t if t.nonEmpty =>
      val (inBattle, queueing) = partition[Battle](multiple)
      val (battle, moving) = commenceBattle(pos, inBattle, queueing)
      val tb = battle match {
        case Some(b) => b.troops.map(x => x -> b)
        case _ => Vector.empty
      }
      (tb, moving)
    case x => (Vector.empty, x)
  }

  private def partition[By <: Occupation](troops: Vector[TO])(implicit ev: SubTyper[Occupation, By]) = {
    val l = Vector.empty[(Troop, By)]
    val r = Vector.empty[TO]
    troops.foldLeft(l, r)((lr, x) => (x._1, ev.asSub(x._2)) match {
      case (a, Some(o)) => ((a, o) +: lr._1, lr._2)
      case _ => (lr._1, x +: lr._2)
    })
  }

  private def commenceBattle(pos: Coordinate, inBattle: Vector[TB], queueing: Vector[TO]): (Option[Battle], Vector[TO]) = inBattle match {
    case (_, b) +: _ =>
      val (inProcess, leaving) = b.tick
      inProcess match {
        case Some(battle) => val (toBattle, byPass) = queueing.partition(x => x._1.isAbleToFight)
          (Some(battle.addTroops(toBattle)), leaving ++ byPass)
        case None if Combatants.isBattleNeeded(queueing.unzip._1) => (Some(Battle(pos, NESeq(queueing))), leaving)
        case _ => (None, leaving ++ queueing)
      }
    case _ if Combatants.isBattleNeeded(queueing.unzip._1) => Battle(pos, NESeq(queueing)).tick
    case _ => (None, queueing)
  }

  //TODO send troop back to city upon arriving
  private def arrive(troops: Iterable[TO]): (Vector[TO], Vector[Troop]) = troops.foldLeft(Vector.empty[TO], Vector.empty[Troop]) { (res, x) =>
    val now = System.currentTimeMillis()
    x match {
      case (fallen, m: Movement) if !fallen.isActive && m.pos(10, now) == m.to => (res._1, fallen +: res._2)
      case (alive, m: Movement) if m.pos(alive.moveSpeed, now) == m.to => ((alive, new Waiting(m.to, now)) +: res._1, res._2)
      case _ => (x +: res._1, res._2)
    }
  }
}
