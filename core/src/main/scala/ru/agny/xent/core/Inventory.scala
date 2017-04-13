package ru.agny.xent.core

import ru.agny.xent.core.Item.ItemId

trait Inventory[T <: Item] {
  val slots: Vector[Slot[T]]
  val self: InventoryLike[Inventory[T], T]

  def add[S <: Inventory[T], U <: T](v: U)(implicit ev: InventoryLike[S, T], ev2: ItemMatcher[T, U]): (S, Slot[T]) = v match {
    case i: SingleItem => (ev.apply(ItemSlot(v) +: slots), EmptySlot)
    case r: StackableItem => getSlot(r.id) match {
      case is@ItemSlot(x) =>
        val (newV, remainder) = is.set(v)
        (ev.apply(slots.updated(slots.indexOf(is), newV)), remainder)
      case EmptySlot => (ev.apply(ItemSlot(v) +: slots), EmptySlot)
    }
    case _ => (ev.apply(slots), ItemSlot(v))
  }

  def set[S <: Inventory[T]](idx: Int, v: Slot[T])(implicit ev: InventoryLike[S, T]): (S, Slot[T]) = {
    val replaced = slots(idx)
    val res = slots.updated(idx, v)
    (ev.apply(res), replaced)
  }

  def move[S <: Inventory[U], U <: Item](idx: Int, to: InventoryLike[S, U])
                                        (implicit ev1: InventoryLike[Inventory[T], T] = implicitly(self),
                                         ev2: InventoryLike[S, U] = to,
                                         ev3: ItemMatcher[U, U],
                                         ev4: ItemLike[U, T],
                                         ev5: ItemLike[T, U]): (Inventory[T], S) = ev1.getByIdx(idx) match {
    case Some(v) => ev4.cast(v) match {
      case (Some(x), Some(y)) =>
        val (toInv, old) = ev2.add(y)
        val (fromInv, _) = ev1.set(idx, old)
        (fromInv, toInv)
      case _ => (ev1.apply(slots), ev2.apply(ev2.slots))
    }

    case _ => (ev1.apply(slots), ev2.apply(ev2.slots))
  }

  def getByIdx(idx: Int): Option[T] = if (slots.isDefinedAt(idx)) Some(slots(idx).get) else None

  def getSlot(id: ItemId): Slot[T] = slots.find(s => !s.isEmpty && s.get.id == id).getOrElse(EmptySlot)

  def getItem(id: ItemId): Option[T] = slots.find(s => !s.isEmpty && s.get.id == id).map(_.get)

  def resources: Vector[ResourceUnit] = slots.flatMap(x => x match {
    case ItemSlot(v) => v match {
      case ru: ResourceUnit => Some(ru)
      case _ => None
    }
    case EmptySlot => None
  })

}

trait InventoryLike[+S <: Inventory[T], T <: Item] extends Inventory[T] {
  implicit val s: S
  override implicit val self: InventoryLike[Inventory[T], T] = this

  def apply(slots: Vector[Slot[T]]): S
}