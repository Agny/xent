package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Property
import ru.agny.xent.core.Producible

trait Equippable extends Producible {
  val name: String
  val attrs: Vector[Property]
}

//object Equippable {
//  def as[T <: Equippable](v: T): Option[T] = v match {
//    case x: Armor => Some(x)
//    case x: Accessory => Some(x)
//    case x: Weapon => Some(x)
//    case _ => None
//  }
//}