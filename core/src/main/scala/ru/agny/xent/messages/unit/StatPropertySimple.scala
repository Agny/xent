package ru.agny.xent.messages.unit

import ru.agny.xent.core.unit.{Characteristic, Level}
import ru.agny.xent.core.unit.equip.StatProperty

case class StatPropertySimple(prop: Characteristic, level: Int) {
  def lift = StatProperty(prop, Level(level, 0))
}
