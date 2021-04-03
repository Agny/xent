package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{DestructibleObject, MapObject, Storage}
import ru.agny.xent.realm.Hexagon
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.war.Defence

case class City(
  id: ItemId,
  owner: Option[PlayerId],
  buildings: Buildings,
  defence: Defence,
  pos: Hexagon
) extends DestructibleObject {

  override def tick(time: TimeInterval) = {
    //TODO tick something!
    this
  }

  override def isEliminated() = owner.isEmpty
}
