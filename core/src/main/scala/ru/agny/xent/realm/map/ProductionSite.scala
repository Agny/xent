package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{DestructibleObject, MapObject, Resource, Storage}
import ru.agny.xent.realm.{Hexagon, Progress}
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.war.Defence

case class ProductionSite(
  id: ItemId,
  owner: Option[PlayerId],
  resource: Resource,
  defence: Defence,
  progress: Progress,
  pos:Hexagon
) extends DestructibleObject {

  override def tick(time: TimeInterval) = {
    ???
  }

  override def isEliminated() = ???
}
