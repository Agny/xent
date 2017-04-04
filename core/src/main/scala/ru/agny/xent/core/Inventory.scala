package ru.agny.xent.core

trait Inventory[T <: Item] {

  val slots: Seq[Slot[T]]

  def add(v: T): (Inventory[T], Slot[T])

  def set(idx: Int, v: Slot[T]): (Inventory[T], Slot[T])

  def move[U <: Item](idx: Int, to: Inventory[U]): (Inventory[T], Inventory[U]) = get(idx) match {
    case Some(v) if to.isMoveAcceptable(v) =>
      val (toInv, old) = to.add(v.asInstanceOf[U])
      val (fromInv, _) = set(idx, old.asInstanceOf[Slot[T]])
      (fromInv, toInv)
    case _ => (this, to)
  }

  def isMoveAcceptable[U <: Item](v: U): Boolean

  def get(idx: Int): Option[T] = slots.isDefinedAt(idx) match {
    case true if !slots(idx).isEmpty => Some(slots(idx).get)
    case _ => None
  }

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
  def apply(slots: Seq[Slot[WeaponItem]]): EquipmentInventory = {
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