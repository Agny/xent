package ru.agny.xent

import ru.agny.xent.Player.AIEnemy

case class Player(
  id: PlayerId
) {
  def isFriendly(other: Player): Boolean = other.id match {
    case AIEnemy.id => false
    case this.id => true
    case _ => false
  }
}

object Player {
  val AIEnemy = Player(PlayerId.AIEnemy)
  val defaultPS = new DefaultPlayerService()

  def isHostile(a: PlayerId, b: PlayerId)(using ps: PlayerService): Boolean = ps.isHostile(a, b)
}
