package ru.agny.xent.messages.unit

import ru.agny.xent.core.unit.{Characteristic, Level}
import ru.agny.xent.core.unit.equip.StatProperty

case class StatPropertySimple(prop: String, level: Int) {
  def lift = {
    Characteristic.from(prop) match {
      case Some(v) => StatProperty(v, Level(level, 0))
      case None => throw new UnsupportedOperationException(s"No Characteristic for $prop")
    }
  }
}
