package ru.agny.xent.core

import ru.agny.xent.core.Item.ItemId

trait Inventory[T <: Item] {
  val slots: Seq[Slot[T]]

  def add[S <: Inventory[T], U <: T](v: U)(implicit ev: InventoryLike[S, T]): (S, Slot[T]) = v match {
    case i: SingleItem => (ev.apply(slots :+ ItemSlot(i).asInstanceOf[Slot[T]]), EmptySlot)
    case r@ResourceUnit(st, id) => getSlot(id) match {
      case is@ItemSlot(x) =>
        val (newV, remainder) = is.set(v)
        (ev.apply(slots.updated(slots.indexOf(is), newV)), remainder)
      case EmptySlot => (ev.apply(slots :+ ItemSlot(v)), EmptySlot)
    }
    case _ => (ev.apply(slots), ItemSlot(v))
  }

  def set[S <: Inventory[T]](idx: Int, v: Slot[T])(implicit ev: InventoryLike[S, T]): (S, Slot[T]) = {
    val replaced = slots(idx)
    val res = slots.updated(idx, v)
    (ev.apply(res), replaced)
  }

  def move[S1 <: Inventory[T], S2 <: Inventory[U], U <: Item]
  (idx: Int)(implicit ev1: InventoryLike[S1, T], ev2: InventoryLike[S2, U]): (S1, S2) = getByIdx(idx) match {
    case Some(v) if ev2.isMoveAcceptable(v) =>
      val (toInv, old) = ev2.add(v.asInstanceOf[U])
      val (fromInv, _) = ev1.set(idx, old.asInstanceOf[Slot[T]])
      (fromInv, toInv)
    case _ => (ev1.apply(slots), ev2.apply(ev2.slots))
  }

  def isMoveAcceptable[U <: Item](v: U): Boolean

  def getByIdx(idx: Int): Option[T] = if (slots.isDefinedAt(idx)) Some(slots(idx).get) else None

  def getSlot(id: ItemId): Slot[T] = slots.find(s => !s.isEmpty && s.get.id == id).getOrElse(EmptySlot)

  def getItem(id: ItemId): Option[T] = slots.find(s => !s.isEmpty && s.get.id == id).map(_.get)

  def resources: Seq[ResourceUnit] = slots.flatMap(x => x match {
    case ItemSlot(v) => v match {
      case ru: ResourceUnit => Some(ru)
      case _ => None
    }
    case EmptySlot => None
  })

}

trait InventoryLike[S <: Inventory[T], T <: Item] extends Inventory[T] {
  implicit val s: S

  def apply(slots: Seq[Slot[T]]): S
}

/*
* Example of implementation
*
case class EquipmentInventory(mainH: TestMainWpn, offHand: TestOffWpn) extends Inventory[WeaponItem] {
  val slots = Seq.empty // don't bother

  override def add(v: WeaponItem): (Inventory[WeaponItem], Slot[WeaponItem]) = set(-1, ItemSlot(v))

  override def set(idx: Int, v: Slot[WeaponItem]): (Inventory[WeaponItem], Slot[WeaponItem]) = v match {
    case ItemSlot(item) => item match {
      case itm@TestMainWpn(_, _) => (EquipmentInventory(itm, offHand), ItemSlot(mainH))
      case itm@TestOffWpn(_, _) => (EquipmentInventory(mainH, itm), ItemSlot(offHand))
    }
    case EmptySlot => get(idx) match {
      case Some(wpn) => (EquipmentInventory(slots.filter(_ != slots(idx))), ItemSlot(wpn))
      case None => (this, EmptySlot)
    }
  }

  override def isMoveAcceptable[U <: Item](v: U): Boolean = v.isInstanceOf[WeaponItem]
}

object EquipmentInventory {
  def copy(slots: Seq[Slot[WeaponItem]]): EquipmentInventory = {
    slots match {
      case ItemSlot(mainW: TestMainWpn) :: ItemSlot(offW: TestOffWpn) :: Nil => EquipmentInventory(mainW, offW)
      case ItemSlot(offW: TestOffWpn) :: ItemSlot(mainW: TestMainWpn) :: Nil => EquipmentInventory(mainW, offW)
    }
  }
}

case class UserInventory(slots: Seq[Slot[Item]]) extends Inventory[Item] {
  override def add(v: Item): (Inventory[Item], Slot[Item]) = ???

  override def set(idx: Int, v: Slot[Item]): (Inventory[Item], Slot[Item]) = ???

  override def isMoveAcceptable[U <: Item](v: U): Boolean = v.isInstanceOf[Item]
}

object TestW extends App {
  def tt: Unit = {
    val ei = EquipmentInventory(Seq(ItemSlot(TestMainWpn(1, "t1")), ItemSlot(TestOffWpn(2, "o2"))))
    val ui = UserInventory(Seq(ItemSlot(ResourceUnit(5, 4)), ItemSlot(TestOffWpn(2, "o3"))))
    val (nui, nei) = ui.move(1, ei)
    println(nui)
    println(nei)
  }

  tt
}

trait WeaponItem extends SingleItem {
  val v: String
}
case class TestMainWpn(id: ItemId, v: String) extends WeaponItem
case class TestOffWpn(id: ItemId, v: String) extends WeaponItem
  */