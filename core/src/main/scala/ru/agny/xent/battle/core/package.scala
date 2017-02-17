package ru.agny.xent.battle.core

sealed trait Attribute
trait Kinetic extends Attribute
trait Magic extends Attribute
trait Summon extends Attribute
//trait Unique extends Attribute

package object attributes {
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
