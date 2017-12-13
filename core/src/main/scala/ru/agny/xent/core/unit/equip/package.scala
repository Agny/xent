package ru.agny.xent.core.unit.equip

sealed trait Attribute
sealed trait Kinetic extends Attribute
sealed trait Magic extends Attribute
sealed trait Summon extends Attribute
//trait Unique extends Attribute

object Attribute {

  val kinetic = Vector(Piercing, Blunt, Slashing, SlashingS, Projectile, Firearm, Siege)
  val magic = Vector(Fire, Water, Earth, Wind, Light, Dark, Void)
  val summon = Vector(SummonSpirit, SummonThing)
  val all = kinetic ++ magic ++ summon

  def from(name: String): Option[Attribute] = all.find(_.toString == name)

  case object Piercing extends Kinetic
  case object Blunt extends Kinetic
  case object Slashing extends Kinetic
  case object SlashingS extends Kinetic
  case object Projectile extends Kinetic
  case object Firearm extends Kinetic
  case object Siege extends Kinetic

  case object Fire extends Magic
  case object Water extends Magic
  case object Earth extends Magic
  case object Wind extends Magic
  case object Light extends Magic
  case object Dark extends Magic
  case object Void extends Magic
  case object SummonSpirit extends Summon
  case object SummonThing extends Summon
}
