package ru.agny.xent.war

import ru.agny.xent._
import ru.agny.xent.TimeInterval._
import ru.agny.xent.realm.map.Troops
import ru.agny.xent.{Player, PlayerId}

case class Sides(troops: Map[PlayerId, Seq[Troops]])(using ps: PlayerService) {

  import Sides._

  private var hostility: Map[PlayerId, Seq[PlayerId]] = hostilityMap(troops.keys.toSeq)

  //TODO rethink formulae
  def getRoundLength(): TimeInterval = {
    val trw = troops.map { case (p, tr) =>
      p -> tr.map(_.weight).sum
    }
    val k = hostility.map { case (k, v) =>
      val ls = trw(k)
      var rs = v.map(trw).sum
      if (ls <= rs) ls / rs.toDouble
      else rs / ls.toDouble
    }.max
    (TimeInterval.BaseRound + TimeInterval.BaseRound * k).toInterval
  }

  def round(): Sides = {
    //TODO use tactics
    troops.foreach { case (p, tr) =>
      tr.filter(_.isAbleToFight()).foreach { att => //each troop of same player attacks once one after one
        //TODO intelligent targeting
        getTargetable(p, troops).headOption.foreach { tgt => // until there is no one to attack or to be attacked
          att.attack(tgt)
        }
      }
    }
    this
  }

  def isTherePlaceForOther(p: PlayerId): Boolean = {
    troops.keys.exists(Player.isHostile(_, p))
  }

  def reinforce(v: (PlayerId, Seq[Troops])) = copy(troops + v)

  private def getTargetable(attacker: PlayerId, troops: Map[PlayerId, Seq[Troops]]): Seq[Troops] = {
    val enemies = hostility(attacker)
    enemies.collect {
      case e => troops(e).filter(_.isAbleToFight())
    }.flatten
  }
}

object Sides {

  /**
   * Build "hostility" map i.e. map of war relations between each and other players
   *
   * @param players to build a map of
   * @return relations map
   */
  private def hostilityMap(players: Seq[PlayerId])(using ps: PlayerService): Map[PlayerId, Seq[PlayerId]] = {
    var hostility = Map.empty[PlayerId, Seq[PlayerId]]
    players.zipWithIndex.map { case (p1, idx) =>
      players.drop(idx + 1).map { p2 =>
        if (Player.isHostile(p1, p2)) {
          hostility = hostility.updatedWith(p2) {
            case Some(v) => Some(p1 +: v)
            case None => Some(Seq(p1))
          }.updatedWith(p1) {
            case Some(v) => Some(p2 +: v)
            case None => Some(Seq(p2))
          }
        }
      }
    }
    hostility
  }
}
