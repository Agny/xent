package ru.agny.xent

import ru.agny.xent.core.User
import ru.agny.xent.core.utils.UserType._
import ru.agny.xent.core.city.City
import ru.agny.xent.core.unit._
import ru.agny.xent.core.unit.equip.Equipment

object TestHelper {

  def defaultSoul(id: ObjectId, eq: Equipment = Equipment.empty): Soul = Soul(id, SoulData(Level(1, 1), 1, Stats.default, Vector.empty), eq)

  def defaultUser(id: UserId = -1, city: City = City.empty(0, 0)): User = User(id, "Test", city)

  val defaultWeight = 10

}
