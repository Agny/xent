package ru.agny.xent.realm.map

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{DestructibleObject, MapObject, Storage, TemporalObject}
import ru.agny.xent.realm.{Hexagon, Progress}
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.war.{Defence, Sides}

case class Battle(
  id: ItemId,
  owner: Option[PlayerId] = None,
  sides: Sides,
  passerbyers: Seq[Troops],
  progress: Progress,
  pos: Hexagon
) extends TemporalObject {
  override val weight = MapObject.NotMovable

  override def tick(time: TimeInterval) = {
    ???
  }

  def end(): Seq[Troops] = {
    //sides, passerbyers
    ???
  }
}

object Battle {
  case class Preparation(success: Seq[Battle], passerbyers: Seq[TemporalObject])

  def build(
    actors: Seq[TemporalObject],
    places: Seq[DestructibleObject]): Preparation = {
    ???
  }
}
