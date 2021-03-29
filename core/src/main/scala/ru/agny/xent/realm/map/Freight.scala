package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{MovingObject, Resource, Storage}
import ru.agny.xent.realm.Movement
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.war.{Defence, Sides}

case class Freight(
  id: ItemId,
  owner: Option[PlayerId],
  cargo: Seq[Resource],
  guard: Troops,
  movement: Movement
) extends MovingObject {
  override val weight = guard.weight

  override def tick(time: TimeInterval) = {
    ???
  }

  override def pos = movement.pos
}
