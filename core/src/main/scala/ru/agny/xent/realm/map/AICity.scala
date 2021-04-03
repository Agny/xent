package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.item.{DestructibleObject, MapObject, Storage}
import ru.agny.xent.realm.Hexagon
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.war.Defence

case class AICity(
  id: ItemId,
  owner: Option[PlayerId] = Some(AIEnemy.id),
  techTier: TechonologyTier,
  defence: Defence,
  storage: Storage,
  pos: Hexagon
) extends DestructibleObject {

  override def tick(time: TimeInterval) = {
    //TODO tick something!
    this
  }

  override def isEliminated() = {
    defence.isEliminated()
  }
}

