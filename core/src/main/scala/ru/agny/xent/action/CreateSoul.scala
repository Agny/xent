package ru.agny.xent.action

import ru.agny.xent.core.User
import ru.agny.xent.core.unit._
import ru.agny.xent.core.unit.equip.{Equipment, StatProperty}
import ru.agny.xent.core.utils.{ErrorCode, ItemIdGenerator}
import ru.agny.xent.messages.{ReactiveLog, ResponseOk}

//TODO skills assignment
case class CreateSoul(spirit: Spirit, stats: Vector[StatProperty], src: ReactiveLog) extends UserAction {
  override def run(user: User) = {
    val requiredPower = stats.map(_.toLifePower).sum + spirit.toLifePower
    user.power.spend(requiredPower) match {
      case Some(v) =>
        val data = SoulData(Level.start, spirit.points, Stats(stats, spirit.base), Vector.empty)
        val soul = Soul(ItemIdGenerator.next, data, Equipment.empty)
        src.respond(ResponseOk)
        user.copy(power = v, souls = user.souls.addNew(soul, user.city.c))
      case None => src.failed(ErrorCode.NOT_ENOUGH_LIFEPOWER); user
    }
  }
}
