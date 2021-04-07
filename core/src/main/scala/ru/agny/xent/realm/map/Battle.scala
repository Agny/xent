package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{DestructibleObject, MapObject, Storage, TemporalObject}
import ru.agny.xent.realm.{Hexagon, Progress, map}
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.utils.ItemIdGenerator
import ru.agny.xent.war.{Defence, Sides}

import scala.annotation.tailrec

case class Battle(
  id: ItemId,
  private var sides: Sides,
  private var progress: Progress,
  pos: Hexagon
) extends TemporalObject {
  override val weight = MapObject.NotMovable
  override val owner = PlayerId.Neutral

  override def tick(time: TimeInterval) = {
    if (progress.fill(time)) {
      sides = sides.round()
      progress.updateCap(sides.getRoundLength())
      this
    } else {
      this
    }
  }

  def isFinished(): Boolean = {
    sides.getRoundLength() == TimeInterval.Zero
  }

  def complete(): Seq[Troops] = {
    sides.troops.values.flatten.toSeq
  }

  def isAbleToParticipate(psb: PlayerId): Boolean = {
    sides.isTherePlaceForOther(psb)
  }

  def reinforce(troops: (PlayerId, Seq[Troops])): Battle = {
    sides = sides.reinforce(troops)
    this
  }
}

object Battle {
  case class State(battles: Seq[Battle], nonParticipants: Seq[Troops])

  def build(
    actors: Seq[TemporalObject],
    places: Seq[DestructibleObject]): State = {
    //TODO apply modifiers from cities/defence
    val (battles: Seq[Battle]@unchecked, troops: Seq[Troops]@unchecked) = actors.partition {
      case b: Battle => true
      case t: Troops => false
    }
    val State(newBattles, tr) = formBattle(troops.groupBy(_.owner), State(Seq.empty, Seq.empty))
    val State(oldBattles, nonParticipants) = joinBattle(tr.groupBy(_.owner), State(battles, Seq.empty))
    State(newBattles ++ oldBattles, nonParticipants)
  }

  /**
   * Here decision is made - to start a fight or evade it.
   *
   * The fight will be started if players hostile toward each other
   *
   * @param actors troops and battles in same Hexagon
   * @param state  builded state of relations
   * @return state
   */
  @tailrec
  private def formBattle(actors: Map[PlayerId, Seq[Troops]], state: State): State = {
    actors.headOption match {
      case Some(v@(pid, troops)) =>
        val (hostile, other) = actors.tail.partition { case (x, _) => Player.isHostile(pid, x) }
        if (hostile.isEmpty) {
          val updatedState = state.copy(nonParticipants = troops ++: state.nonParticipants)
          formBattle(actors.tail, updatedState)
        } else {
          val b = Battle(ItemIdGenerator.next, Sides(hostile + v), Progress.Start(), troops.head.pos)
          val updatedState = state.copy(battles = b +: state.battles)
          formBattle(other, updatedState)
        }
      case None => state
    }
  }

  /**
   * Here decision is made - to join a fight or move on.
   *
   * Troops will have to join if
   * - battle has place for them to join
   * - they are eager for battle //TODO
   * - there is atleast one enemy troops present //TODO
   *
   * @param actors troops and battles in same Hexagon
   * @param state  builded state of relations
   * @return state
   */
  private def joinBattle(actors: Map[PlayerId, Seq[Troops]], state: State): State = {
    actors.headOption match {
      case Some(v@(pid, troops)) =>
        val (participable, other) = state.battles.partition(_.isAbleToParticipate(pid))
        participable.headOption match {
          case Some(value) =>
            val updated = value.reinforce(v)
            val updatedState = state.copy(battles = updated +: other ++: participable.tail)
            joinBattle(actors.tail, updatedState)
          case None =>
            val updatedState = state.copy(nonParticipants = troops ++: state.nonParticipants)
            joinBattle(actors.tail, updatedState)
        }
      case None => state
    }
  }
}
