package ru.agny.xent.battle

import ru.agny.xent.battle.core.{Summon, Magic, Kinetic}

package object attributes {
  case class Piercing(value: Int) extends Kinetic
  case class Blunt(value: Int) extends Kinetic
  case class Slashing(value: Int) extends Kinetic
  case class SlashingS(value: Int) extends Kinetic
  case class Projectile(value: Int) extends Kinetic
  case class Firearm(value: Int) extends Kinetic
  case class Siege(value: Int) extends Kinetic

  case class Fire(value: Int) extends Magic
  case class Water(value: Int) extends Magic
  case class Earth(value: Int) extends Magic
  case class Wind(value: Int) extends Magic
  case class Light(value: Int) extends Magic
  case class Dark(value: Int) extends Magic
  case class Void(value: Int) extends Magic
  case class SummonSpirit(value: Int, power: Int) extends Summon
  case class SummonThing(value: Int, power: Int) extends Summon
}
