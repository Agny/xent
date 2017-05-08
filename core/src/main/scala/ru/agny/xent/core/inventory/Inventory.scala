package ru.agny.xent.core.inventory

import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.{Item, SingleItem, StackableItem}

trait Inventory[S <: Inventory[_, T], T <: Item] {
  val holder: SlotHolder[T]
  val self: InventoryLike[S, T]

  protected def add[U <: T](v: U)(implicit ev: ItemMerger[T, U]): (S, Slot[T]) = v match {
    case i: SingleItem => (self.apply(ItemSlot(v) +: holder.slots), EmptySlot)
    case r: StackableItem => getSlot(r.id) match {
      case is@ItemSlot(x) =>
        is.set(v) match {
          case Some((newValue, remainder)) => (self.apply(holder.slots.updated(holder.slots.indexOf(is), newValue)), remainder)
          case _ => (self.asInventory, is)

        }
      case EmptySlot => (self.apply(ItemSlot(v) +: holder.slots), EmptySlot)
    }
  }

  def set(idx: Int, v: Slot[T])(implicit ev: ItemMerger[T, T]): (S, Slot[T]) = {
    v match {
      case ItemSlot(s) => holder.slots(idx).set(s) match {
        case Some((newValue, oldValue)) => (self.apply(holder.slots.updated(idx, newValue)), oldValue)
        case None => (self.asInventory, v)
      }
      case e@EmptySlot =>
        val oldValue = holder.slots(idx)
        (self.apply(holder.slots.diff(Vector(oldValue))), oldValue)
    }
  }

  def move[S2 <: Inventory[_, U], U <: Item](idx: Int, to: InventoryLike[S2, U])
                                            (implicit ev1: ItemSubChecker[T, U],
                                             ev2: ItemLike[T, U],
                                             ev3: ItemMerger[U, U],
                                             ev4: ItemMerger[T, T]): (S, S2) = {
    implicit val ths = implicitly(self)
    implicit val that = implicitly(to)
    ths.getByIdx(idx) match {
      case Some(v) => ev1.asSub(v) match {
        case Some(y) =>
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

trait InventoryLike[S <: Inventory[_, T], T <: Item] extends Inventory[S, T] {
  val asInventory: S
  override val self: InventoryLike[S, T] = this

  def apply(slots: Vector[Slot[T]]): S
}