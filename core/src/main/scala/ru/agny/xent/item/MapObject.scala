package ru.agny.xent.item

import ru.agny.xent.ItemWeight
import ru.agny.xent.realm.Hexagon

sealed trait MapObject extends Item with Mutable {
  def pos: Hexagon
}
trait DestructibleObject extends MapObject {
  override val weight = MapObject.NotMovable
  def isEliminated(): Boolean
}
trait MovingObject extends MapObject

object MapObject {
  val NotMovable: ItemWeight = Int.MaxValue
}
