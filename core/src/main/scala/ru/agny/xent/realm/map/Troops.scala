package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{Backpack, TemporalObject, Storage}
import ru.agny.xent.realm.Movement
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.unit.Soul
import ru.agny.xent.war.{Defence, Fatigue, Sides}

case class Troops(
  id: ItemId,
  owner: PlayerId,
  backpack: Backpack,
  units: Seq[Soul],
  movement: Movement,
  fatigue: Fatigue
) extends TemporalObject {
  override def weight = activeSouls().map(_._2.weight).sum

  override def tick(time: TimeInterval) = {
    movement.tick(velocity(), time)
    this
  }

  override def pos = movement.pos

  def velocity(): Velocity = {
    units.map(_.velocity()).min
  }

  def isAggressive(): Boolean = true

  def isAbleToFight(): Boolean = activeSouls().nonEmpty

  def activeSouls(): Seq[Soul] = units.filter(_.state() == Soul.State.Active)

  def attack(that: Troops): Unit = {
    activeSouls().foreach { x =>
      //TODO targeting!
      x.attack(that)
    }
  }

  //TODO damage attributes
  def takeDamage(damage: Int): Unit = {
    activeSouls().foreach(s => s.takeDamage(damage))
  }
}
