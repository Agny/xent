package ru.agny.xent.realm

import ru.agny.xent._

trait Item {
  val id: ItemId
  val owner: Option[PlayerId]
  val weight: ItemWeight
}
