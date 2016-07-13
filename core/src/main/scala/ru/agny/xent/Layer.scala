package ru.agny.xent

import ru.agny.xent.core.{CellsMap, WorldCell}
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Seq[User], map: CellsMap[WorldCell], facilities: List[FacilityTemplate]) {

  def tick(action: (Message, Action)): Layer = {
    val (acted, idle) = users.partition(x => x.id == action._1.user)
    val (msg, a) = action
    a match {
      case ua: UserAction =>
        val updated = acted.map(x => x.work(msg, ua))
        Layer(id, level, idle ++ updated, map, facilities)
    }
  }

  def updateMap(cell: WorldCell): Layer = {
    Layer(id, level, users, map.update(cell), facilities)
  }
}
