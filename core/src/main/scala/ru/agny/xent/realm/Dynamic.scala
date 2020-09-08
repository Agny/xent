package ru.agny.xent.realm

import ru.agny.xent._

enum Dynamic extends Item {
  case Battle(
      id: ItemId = ???,
      owner: Option[PlayerId] = ???,
      weight: ItemWeight = ???
      //TODO      sides: Sides,
      //          passerbyers: Seq[Troops],
      //          progress: Progress
  )
//  case Freight(
      //TODO      owner: Player,
      //          cargo: Seq[Resource],
      //          movement: Movement
//  )
//  case Troops(
      //TODO      owner: Player,
      //          backpack: Backpack,
      //          units: Seq[Soul],
      //          movement: Movement,
      //          fatigue: Fatigue
//  )
}
