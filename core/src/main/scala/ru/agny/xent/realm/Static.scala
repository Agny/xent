package ru.agny.xent.realm

import ru.agny.xent.realm.Dynamic._
import ru.agny.xent._

enum Static extends Item {
  case BarbarianCamp(
      id: ItemId = ???,
      owner: Option[PlayerId] = ???,
      weight: ItemWeight = ???
//TODO      owner: Player = Enemy,
//      techTier:TechnologyTier,
//      defence:Defence,
//      resources:Resources
  )
//  case City(
//TODO      owner:Player,
//          buildings:Buildings,
//          defence:Defence
//  )
//  case ProductionSite(
//TODO      owner:Player,
//          resource:Resource,
//          defence:Defence,
//          progress:Progress
//  )
//  case ResourceDeposit(
//TODO      resource:Resource
//  )
  //TODO case Altar
}
