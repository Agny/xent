package ru.agny.xent.realm

import ru.agny.xent._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.item._
import ru.agny.xent.realm.ai.TechonologyTier
import ru.agny.xent.war.Defence
import ru.agny.xent.city.Buildings

enum Static extends MapObject {
  case BarbarianCamp(
      id: ItemId,
      owner: Option[PlayerId] = Some(AIEnemy.id),
      techTier: TechonologyTier,
      defence: Defence,
      storage: Storage
  )
  case City(
      id: ItemId,
      owner: Option[PlayerId],
      buildings: Buildings,
      defence: Defence
  )
  case ProductionSite(
      id: ItemId,
      owner: Option[PlayerId],
      resource: Resource,
      defence: Defence,
      progress: Progress
  )
  case ResourceDeposit(
      id: ItemId,
      owner: Option[PlayerId] = None,
      resource: Resource
  )
  //TODO case Altar
}
