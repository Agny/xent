package ru.agny.xent

import ru.agny.xent.UserType._
import ru.agny.xent.core.unit._
import ru.agny.xent.core.unit.equip.Equipment

object TestHelper {

  def defaultSoul(id: ObjectId, eq: Equipment = Equipment.empty): Soul = Soul(id, SoulData(Level(1, 1), 1, Stats.default, Vector.empty), eq)

}
