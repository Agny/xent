package ru.agny.xent.core.inventory

import ru.agny.xent.core.unit.equip.DefaultValue

sealed trait Slot[+T <: Item] {
  def get: T

  def isEmpty: Boolean

  def set[U <: Item](v: U)(implicit ev: ItemMerger[T, U]): Option[(Slot[U], Slot[T])] = ev.asCompatible(get, v) match {
    case Some(x) => Some((ItemSlot(x), this))
    case None => None
  }

  def getOrElse[U >: T](default: => U): U = if (isEmpty) default else get

  def flatten: Option[T] = if (!isEmpty) get match {
    case x: DefaultValue[_] => None
    case v => Some(v)
  } else None

}
object Slot {
  implicit def convert[To <: Item, From <: Item](v: Slot[From])(implicit ev: ItemLike[To, From]): Slot[To] = v match {
    case ItemSlot(x) => ev.cast(x) match {
      case (Some(a), Some(b)) => ItemSlot(b)
      case _ => EmptySlot
    }
    case _ => EmptySlot
  }
}

final case class ItemSlot[T <: Item](v: T) extends Slot[T] {
  def get = v

  def isEmpty = false
}

case object EmptySlot extends Slot[Nothing] {
  def get = throw new NoSuchElementException("EmptySlot.get")

  def isEmpty = true
}
