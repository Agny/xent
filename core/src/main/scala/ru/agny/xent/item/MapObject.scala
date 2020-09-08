package ru.agny.xent.item

import ru.agny.xent.ItemWeight

trait MapObject extends Item {
  val NotMovable: ItemWeight = Int.MaxValue
  override val weight: ItemWeight = NotMovable
}
