package ru.agny.xent.core

trait ItemMatcher[-From <: Item, To <: Item] {
  def toStack(a: From, b: To): Option[To]
}

object ItemMatcher {
  object implicits {
    implicit object ResourceMatcher extends ItemMatcher[Item, Item] {
      override def toStack(a: Item, b: Item): Option[ResourceUnit] = (a, b) match {
        case (toSet: ResourceUnit, current: ResourceUnit) => Some(current.add(toSet))
        case _ => None
      }
    }

    implicit object ExtractableMatcher extends ItemMatcher[Extractable, Extractable] {
      override def toStack(a: Extractable, b: Extractable): Option[Extractable] = None
    }
  }
}
