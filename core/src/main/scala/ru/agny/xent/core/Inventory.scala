package ru.agny.xent.core

import ru.agny.xent.core.Item.ItemId

trait Inventory[T <: Item] {
  val holder: SlotHolder[T]
  val self: InventoryLike[Inventory[T], T]

  protected def add[S <: Inventory[T], U <: T](v: U)
                                    (implicit ev: InventoryLike[S, T],
                                     ev2: ItemMatcher[T, U]): (S, Slot[T]) = v match {
    case i: SingleItem => (ev.apply(ItemSlot(v) +: holder.slots), EmptySlot)
    case r: StackableItem => getSlot(r.id) match {
      case is@ItemSlot(x) =>
        val (newV, remainder) = is.set(v)
        (ev.apply(holder.slots.updated(holder.slots.indexOf(is), newV)), remainder)
      case EmptySlot => (ev.apply(ItemSlot(v) +: holder.slots), EmptySlot)
    }
  }

  def set(idx: Int, v: Slot[T]): (Inventory[T], Slot[T]) = {
    val replaced = holder.slots(idx)
    val res = holder.slots.updated(idx, v)
    (self.apply(res), replaced)
  }

  def move[S <: Inventory[U], U <: Item](idx: Int, to: InventoryLike[S, U])
                                        (implicit ev1: ItemLike[U, T],
                                         ev2: ItemLike[T, U],
                                         ev3: ItemMatcher[U,U]): (Inventory[T], S) = {
    implicit val ths = implicitly(self)
    implicit val that = implicitly(to)
    ths.getByIdx(idx) match {
      case Some(v) => ev1.cast(v) match {
        case (Some(x), Some(y)) =>
          val (toInv, old) = that.add(y)
          val (fromInv, _) = ths.set(idx, old)
          (fromInv, that.apply(toInv.holder.slots))
        case _ => (ths.apply(holder.slots), that.apply(that.holder.slots))
      }
      case _ => (ths.apply(holder.slots), that.apply(that.holder.slots))
    }
  }

  def getByIdx(idx: Int): Option[T] = if (holder.slots.isDefinedAt(idx)) Some(holder.slots(idx).get) else None

  def getSlot(id: ItemId): Slot[T] = holder.slots.find(s => !s.isEmpty && s.get.id == id).getOrElse(EmptySlot)

  def getItem(id: ItemId): Option[T] = holder.slots.find(s => !s.isEmpty && s.get.id == id).map(_.get)
}

trait InventoryLike[+S <: Inventory[T], T <: Item] extends Inventory[T] {
  override implicit val self: InventoryLike[S, T] = this

  def apply(slots: Vector[Slot[T]]): S
}