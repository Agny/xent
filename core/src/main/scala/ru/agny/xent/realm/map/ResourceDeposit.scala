package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{DestructibleObject, ItemStack, MapObject, Resource, Storage}
import ru.agny.xent.realm.Hexagon
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.utils.ItemIdGenerator
import ru.agny.xent.war.Defence

case class ResourceDeposit(
  id: ItemId,
  resource: Resource,
  private var volume: Int,
  pos: Hexagon
) extends DestructibleObject {
  val owner: PlayerId = PlayerId.Neutral

  override def tick(time: TimeInterval) = this

  def extract(): ItemStack = {
    volume -= 1
    ItemStack(resource, 1)
  }

  def isDepleted(): Boolean = volume <= 0

  override def isEliminated() = isDepleted()
}
