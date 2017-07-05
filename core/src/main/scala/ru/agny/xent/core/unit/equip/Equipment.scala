package ru.agny.xent.core.unit.equip

import ru.agny.xent.core.inventory._
import ru.agny.xent.core.unit.equip.DefaultValue.implicits.DefaultWeapon

case class Equipment(holder: EquippableHolder) extends InventoryLike[Equipment, Equippable] {

  import Equipment._
  import ItemMerger.implicits._

  val asInventory = this

  def weapons = holder.activeItems

  def armor = holder.set.armor

  def accessory = holder.set.accessory

  def toSpoil = holder.set.items.flatMap(_.flatten)

  def weight = holder.items.foldLeft(0)((w, eq) => w + eq.weight)

  def props(wpn: Weapon = DefaultWeapon)(implicit mode: Mode): Vector[AttrProperty] = (mode match {
    case Defensive => holder.items.foldLeft(Map.empty[Attribute, Int])((a, b) =>
      b.attrs.foldLeft(a)(collectAllPotential)
    )
    case Offensive => //TODO second weapon attack potential? Skill|reduced effect|something else
      val wpnAttrs = wpn.attrs.map(x => x.prop -> x.value).toMap
      (wpn +: holder.passiveItems).foldLeft(wpnAttrs)((a, b) =>
        b.attrs.foldLeft(a)(collectSpecifiedPotential)
      )
  }).map { case (attr, power) => AttrProperty(attr, power, mode) }.toVector

  override def add[U <: Equippable](v: U)(implicit ev: ItemMerger[Equippable, U]): (Equipment, Slot[Equippable]) = {
    val idx = holder.getIndexForEquippable(v)
    if (idx < 0) {
      (this, ItemSlot(v))
    } else {
      val (holder, s) = set(idx, ItemSlot(v))
      (holder, s)
    }
  }

  override def set(idx: Int, v: Slot[Equippable])(implicit ev: ItemMerger[Equippable, Equippable]): (Equipment, Slot[Equippable]) = {
    val (updated, out) = holder.set(idx, v)
    if (updated == holder) (this, v)
    else (Equipment(updated), out)
  }

  override def apply(slots: Vector[Slot[Equippable]]): Equipment = Equipment(slots)

}
object Equipment {
  val (mainWeaponIdx, secondaryWeaponIdx, armorIdx, accessoryIdx) = (0, 1, 2, 3)

  val empty: Equipment = Equipment(Vector.empty)

  def apply(slots: Vector[Slot[Equippable]]): Equipment = Equipment(EquippableHolder(EquipmentSet(slots)))

  private def collectAllPotential(attrs: Map[Attribute, Int], prop: AttrProperty)(implicit mode: Mode) = prop.mode match {
    case wanted if mode == wanted =>
      if (attrs.contains(prop.prop)) {
        attrs + (prop.prop -> (attrs(prop.prop) + prop.value))
      } else {
        attrs + (prop.prop -> prop.value)
      }
    case _ => attrs
  }

  private def collectSpecifiedPotential(attrs: Map[Attribute, Int], prop: AttrProperty)(implicit mode: Mode) = prop.mode match {
    case correct if correct == mode && attrs.contains(prop.prop) =>
      attrs + (prop.prop -> (attrs(prop.prop) + prop.value))
    case _ => attrs
  }
}
