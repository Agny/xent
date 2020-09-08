package ru.agny.xent.realm

import ru.agny.xent._
import ru.agny.xent.item.{Backpack, Item, MapObject, Resource}
import ru.agny.xent.war._

enum Dynamic extends MapObject {
  case Battle(
      id: ItemId,
      owner: Option[PlayerId] = None,
      sides: Sides,
      passerbyers: Seq[Troops],
      progress: Progress
  )
  case Freight(
      id: ItemId,
      owner: Option[PlayerId],
      cargo: Seq[Resource],
      movement: Movement
  )
  case Troops(
      id: ItemId,
      owner: Option[PlayerId],
      backpack: Backpack,
      units: Seq[Soul],
      movement: Movement,
      fatigue: Fatigue
  )

  override val weight: ItemWeight = this match {
    case Troops | Freight => ???
    case _ => MapObject.NotMovable
  }
}
