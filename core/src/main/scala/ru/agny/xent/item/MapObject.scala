package ru.agny.xent.item

import ru.agny.xent.ItemWeight
import MapObject._

trait MapObject extends Item {
  override val weight: ItemWeight = NotMovable
}

object MapObject {
  val NotMovable: ItemWeight = Int.MaxValue
}
