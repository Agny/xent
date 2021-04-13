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
  owner: PlayerId,
  deposit: ResourceDeposit,
  defence: Defence,
  storage: Storage,
  pos: Hexagon
) extends DestructibleObject {

  private val progress = Progress(0, deposit.resource.yieldTime)

  override def tick(time: TimeInterval) = {
    if (deposit.isDepleted()) {
      this
    } else {
      if (progress.fill(time)) {
        storage.add(deposit.extract())
      }
      this
    }
  }

  override def isEliminated() = defence.isEliminated()
}
