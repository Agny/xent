package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.battle.core._
import ru.agny.xent.battle.core.attributes.{Blunt, Piercing, Slashing}
import ru.agny.xent.core._

case class Equipment(slots: Vector[Slot[Equippable]]) extends InventoryLike[Equipment, Equippable] {

  import Equipment._
  import DefaultValue.implicits._
  import ItemMatcher2.implicits._

  private val mainHandSlot = extractValue[Weapon](slots)
  private val offHandSlot = extractValue[Weapon](slots.diff(Vector(ItemSlot(mainHandSlot))))
  private val armorSlot = extractValue[Armor](slots)
  private val accessorySlot = extractValue[Accessory](slots)

  def props(wpn: Weapon = DefaultWeapon)(implicit mode: Mode): Vector[Property] = (mode match {
    case Defensive =>
      Vector(mainHandSlot, offHandSlot, armorSlot, accessorySlot).foldLeft(Map.empty[Attribute, Int])((a, b) =>
        b.attrs.foldLeft(a)(collectAllPotential)
      )
    case Offensive =>
      val wpnAttrs = wpn.attrs.map(x => x.attr -> x.value).toMap
      Vector(armorSlot, accessorySlot).foldLeft(wpnAttrs)((a, b) =>
        b.attrs.foldLeft(a)(collectSpecifiedPotential)
      )
  }).map { case (attr, power) => Property(attr, power, mode) }.toVector

  def weapons = Vector(mainHandSlot, offHandSlot)

  def armor = armorSlot

  override def apply(slots: Vector[Slot[Equippable]]): Equipment = Equipment(slots)

}
object Equipment {

  def empty(): Equipment = Equipment(Vector.empty)

  private def extractValue[T <: Equippable](v: Vector[Slot[Equippable]])
                                   (implicit ev: DefaultValue[T], ev2: ItemMatcher2[Equippable, T]): T = getEquip[T](v) match {
    case Vector(a) => a
    case _ => ev.self
  }

  private def getEquip[T <: Equippable](v: Vector[Slot[Equippable]])
                               (implicit matcher: ItemMatcher2[Equippable, T]): Vector[T] =
    v.filter(s => !s.isEmpty).flatMap(s => matcher.toStack(s.get) match {
      case a@Some(x) => a
      case _ => None
    })

  private def collectAllPotential(attrs: Map[Attribute, Int], prop: Property)(implicit mode: Mode) = prop.mode match {
    case correct if correct == mode =>
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
      ItemSlot(WoodenAcc(3, "WoAcc", Vector(Property(Slashing, 28, Offensive), Property(Piercing, 23, Defensive)), ProductionSchema.default()))
    )
    val tW = ItemSlot(WoodenSword(4, "WoSword", Vector(Property(Slashing, 22, Offensive), Property(Piercing, 13, Offensive)), 2 d 6, ProductionSchema.default()))
    val v = Equipment(Vector.empty)
    println(v.weapons)
    println(v.armor)
    val nv = v.apply(slots)
    println(nv.weapons)
    println(nv.armor)
//    val (nnv, wprev) = nv.(tW)
//    println(nnv.weapons)
//    println(nnv.armor)
//    println(wprev)
  }
}

case class WoodenArmor(id: ItemId, name: String, attrs: Vector[Property], value: Int, schema: ProductionSchema) extends Armor
case class WoodenSword(id: ItemId, name: String, attrs: Vector[Property], damage: Dice, schema: ProductionSchema) extends Weapon
case class WoodenAcc(id: ItemId, name: String, attrs: Vector[Property], schema: ProductionSchema) extends Accessory
