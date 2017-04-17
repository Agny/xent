package ru.agny.xent.core.inventory

import ru.agny.xent.core.{ResourceUnit, Item}

trait ItemMerger[-From <: Item, To <: Item] {
  def asCompatible(a: From, b: To): Option[To]
}

object ItemMerger {
  object implicits {
    implicit object ResourceMerger extends ItemMerger[Item, Item] {
      override def asCompatible(a: Item, b: Item): Option[ResourceUnit] = (a, b) match {
        case (toSet: ResourceUnit, current: ResourceUnit) => Some(current.add(toSet))
        case _ => None
      }
    }
  }
}
