package ru.agny.xent

import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.utils.{BuildingTemplate, ItemIdGenerator}
import ru.agny.xent.core._
import ru.agny.xent.core.unit._
import ru.agny.xent.core.unit.equip.{Equipment, StatProperty}

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
      val data = SoulData(Level.start, spirit, Stats(stats), Vector.empty)
      val soul = Soul(ItemIdGenerator.next, data, Equipment.empty)
      user.copy(power = lifePower, souls = user.souls.addNew(soul, user.city.c))
    }
  }
}
