package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{Backpack, MovingObject, Storage}
import ru.agny.xent.realm.Movement
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.unit.Soul
import ru.agny.xent.war.{Defence, Fatigue, Sides}

case class Troops(
  id: ItemId,
  owner: Option[PlayerId],
  backpack: Backpack,
  units: Seq[Soul],
  movement: Movement,
  fatigue: Fatigue
) extends MovingObject {
  override def weight = units.map(_._2.weight).sum

  override def tick(time: TimeInterval) = {
    movement.tick(velocity(), time)
    this
  }

  override def pos = movement.pos

  def velocity(): Velocity = {
    units.map(_.velocity()).min
  }
}
