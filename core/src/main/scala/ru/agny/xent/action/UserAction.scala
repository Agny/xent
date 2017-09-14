package ru.agny.xent.action

import ru.agny.xent.core.city.Building
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.{User, _}
import ru.agny.xent.core.unit._
import ru.agny.xent.core.unit.equip.{Equipment, StatProperty}
import ru.agny.xent.core.utils.{BuildingTemplate, ItemIdGenerator}
import ru.agny.xent.messages.Response

trait UserAction extends Action {
  type T = User

  override def run(user: T): Either[Response, T]
}

object DoNothing extends UserAction {
  override def run(user: User) = Right(user)
}

case class PlaceBuilding(facility: String, layer: Layer, cell: Coordinate) extends UserAction {
  override def run(user: User) = {
    val bt = layer.facilities.collectFirst { case bt: BuildingTemplate if bt.name == facility => bt }
    bt.map(x => {
      val b = Building(x.name, x.producibles, x.buildTime)
      user.build(LocalCell(cell.x, cell.y, Some(b)), x.cost)
    }) getOrElse Left(Response(s"Unable to build $facility"))
  }
}

case class AddProduction(facility: ItemId, res: ItemStack) extends UserAction {
  override def run(user: User) = user.addProduction(facility, res)
}

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
