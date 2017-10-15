package ru.agny.xent.action

import ru.agny.xent.core.User
import ru.agny.xent.core.unit._
import ru.agny.xent.core.unit.equip.{Equipment, StatProperty}
import ru.agny.xent.core.utils.ItemIdGenerator

//TODO skills assignment
case class CreateSoul(spirit: Spirit, stats: Vector[StatProperty]) extends UserAction {
  override def run(user: User) = {
    val requiredPower = stats.map(_.toLifePower).sum + spirit.toLifePower
    for (
      lifePower <- user.power.spend(requiredPower)
    ) yield {
      val data = SoulData(Level.start, spirit.points, Stats(stats, spirit.base), Vector.empty)
      val soul = Soul(ItemIdGenerator.next, data, Equipment.empty)
      user.copy(power = lifePower, souls = user.souls.addNew(soul, user.city.c))
    }
  }
}
