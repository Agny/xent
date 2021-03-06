package ru.agny.xent.core.unit

import ru.agny.xent.core.unit.equip.Attribute
import ru.agny.xent.core.unit.equip.Attribute._

sealed trait Characteristic {
  val name: String = getClass.getSimpleName
  val weightModifier: Int
  protected val relatedAttributesWithModifier: Map[Attribute, Double]

  final def bonusModifier(attr: Attribute): Double = relatedAttributesWithModifier(attr)

  override def toString = name
}

object Characteristic {

  private val all = Vector(Agility, Strength, Intelligence, PresencePower, CritRate, CritPower, Initiative)

  def from(name: String): Option[Characteristic] = all.find(_.name.equalsIgnoreCase(name))

  case object Agility extends Primary {
    val relatedAttributesWithModifier = (Map[Attribute, Double](
      Piercing -> 0.3,
      Slashing -> 0.5,
      SlashingS -> 0.2,
      Projectile -> 0.8,
      Firearm -> 0.3)
      ++ magic.map(_ -> 0.1)).withDefaultValue(0d)
  }
  case object Strength extends Primary {
    val relatedAttributesWithModifier = Map[Attribute, Double](
      Piercing -> 0.7,
      Blunt -> 1.0,
      Slashing -> 0.5,
      SlashingS -> 0.2,
      Projectile -> 0.2,
      Firearm -> 0.1,
      Siege -> 1.5).withDefaultValue(0d)
  }
  case object Intelligence extends Primary {
    val relatedAttributesWithModifier = (Map[Attribute, Double](
      SlashingS -> 0.2,
      Firearm -> 0.1,
      SummonSpirit -> 1.5,
      SummonThing -> 1.2)
      ++ magic.map(_ -> 1.2)).withDefaultValue(0d)
  }
  case object PresencePower extends Primary {
    val relatedAttributesWithModifier = Map[Attribute, Double](
      SummonSpirit -> 0.5,
      SummonThing -> 1.2).withDefaultValue(0d)
  }
  case object CritRate extends Secondary
  case object CritPower extends Secondary
  case object Initiative extends Secondary
}

sealed trait Primary extends Characteristic {
  //TODO game balancing
  val weightModifier = 10
}
sealed trait Secondary extends Characteristic {
  //TODO game balancing
  val weightModifier = 2
  val relatedAttributesWithModifier: Map[Attribute, Double] = Map.empty.withDefaultValue(0d)
}