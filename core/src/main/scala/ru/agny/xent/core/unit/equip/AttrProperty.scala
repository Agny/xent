package ru.agny.xent.core.unit.equip

import ru.agny.xent.core.LifePowered
import ru.agny.xent.core.unit.{Characteristic, Level, Levelable}

sealed trait SoulProperty[T] {
  val prop: T
}

case class AttrProperty(prop: Attribute, value: Int, mode: Mode) extends SoulProperty[Attribute]
case class StatProperty(prop: Characteristic, level: Level) extends SoulProperty[Characteristic] with Levelable with LifePowered {
  def toLifePower: Int = {
    val expBonus = level.exp * prop.weightModifier / level.capacity
    scaledWeight + expBonus
  }

  def bonusDamage(forAttr: Attribute): Int = {
    math.floor(level.tiered.map(x => Dice(x._2, x._1).cast).sum * prop.bonusModifier(forAttr)).toInt
  }

  private def scaledWeight = {
    val step = 10
    val scale = level.value / step
    val tiersWeight = (1 + scale) * (scale * step) * prop.weightModifier / 2
    val levelsWeight = (1 + scale) * (level.value - (scale * step)) * prop.weightModifier
    tiersWeight + levelsWeight
  }
}

sealed trait Mode
object Mode {
  private val all = Vector(Offensive, Defensive)

  def from(name: String): Option[Mode] = all.find(_.toString == name)

  case object Offensive extends Mode
  case object Defensive extends Mode
}
