package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core._
import ru.agny.xent.battle.core.attributes.{Blunt, Piercing, Slashing}
import ru.agny.xent.battle.unit.inventory.DefaultValue.implicits.DefaultWeapon
import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.ProductionSchema
import ru.agny.xent.core.inventory.{EmptySlot, ItemSlot, Slot, InventoryLike}

case class Equipment(holder: EquippableHolder) extends InventoryLike[Equipment, Equippable] {

  import ru.agny.xent.core.inventory.ItemMerger.implicits._
  import Equipment._

  val asInventory = this

  def weapons = holder.activeItems

  def armor = holder.armor

  def props(wpn: Weapon = DefaultWeapon)(implicit mode: Mode): Vector[Property] = (mode match {
    case Defensive => holder.items.foldLeft(Map.empty[Attribute, Int])((a, b) =>
      b.attrs.foldLeft(a)(collectAllPotential)
    )
    case Offensive =>
      val wpnAttrs = wpn.attrs.map(x => x.attr -> x.value).toMap
      holder.passiveItems.foldLeft(wpnAttrs)((a, b) =>
        b.attrs.foldLeft(a)(collectSpecifiedPotential)
      )
  }).map { case (attr, power) => Property(attr, power, mode) }.toVector

  def set[T <: Equippable](idx: Int, v: Slot[Equippable]): (Equipment, Slot[Equippable]) = super.set(idx, v)

  override def apply(slots: Vector[Slot[Equippable]]): Equipment = Equipment(slots)

}
object Equipment {

  def empty(): Equipment = Equipment(Vector.empty)

  def apply(slots: Vector[Slot[Equippable]]): Equipment = Equipment(EquippableHolder(slots))

  private def collectAllPotential(attrs: Map[Attribute, Int], prop: Property)(implicit mode: Mode) = prop.mode match {
    case wanted if mode == wanted =>
      if (attrs.contains(prop.attr)) {
        attrs + (prop.attr -> (attrs(prop.attr) + prop.value))
      } else {
        attrs + (prop.attr -> prop.value)
      }
    case _ => attrs
  }

  private def collectSpecifiedPotential(attrs: Map[Attribute, Int], prop: Property)(implicit mode: Mode) = prop.mode match {
    case correct if correct == mode && attrs.contains(prop.attr) =>
      attrs + (prop.attr -> (attrs(prop.attr) + prop.value))
    case _ => attrs
  }
}

object EqTest extends App {

  import Dice._
  {
    val slots = Vector(
      ItemSlot(WoodenSword(1, "WoSw", Vector(Property(Slashing, 22, Offensive), Property(Piercing, 13, Offensive)), 2 d 6, ProductionSchema.default())),
      ItemSlot(WoodenArmor(2, "WoAr", Vector(Property(Blunt, 12, Defensive), Property(Piercing, 20, Defensive)), 1, ProductionSchema.default())),
      ItemSlot(WoodenAcc(3, "WoAcc", Vector(Property(Slashing, 28, Offensive), Property(Piercing, 23, Defensive)), ProductionSchema.default())),
      ItemSlot(DefaultWeapon)
    )
    val tW = ItemSlot(WoodenSword(4, "WoSword", Vector(Property(Slashing, 22, Offensive), Property(Piercing, 13, Offensive)), 2 d 6, ProductionSchema.default()))
    val v = Equipment(slots)
    println(v.holder)
    val (nv, old) = v.set(1, EmptySlot)
    println(old)
    println(nv.holder)
    //    val (nnv, wprev) = nv.(tW)
    //    println(nnv.weapons)
    //    println(nnv.armor)
    //    println(wprev)
  }
}

case class WoodenArmor(id: ItemId, name: String, attrs: Vector[Property], value: Int, schema: ProductionSchema) extends Armor
case class WoodenSword(id: ItemId, name: String, attrs: Vector[Property], damage: Dice, schema: ProductionSchema) extends Weapon
case class WoodenAcc(id: ItemId, name: String, attrs: Vector[Property], schema: ProductionSchema) extends Accessory
