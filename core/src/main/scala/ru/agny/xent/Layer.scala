package ru.agny.xent

import ru.agny.xent.core.{CellsMap, WorldCell}
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Seq[User], map: CellsMap[WorldCell], facilities: List[FacilityTemplate]) {

  def tick(action: Action): Either[Response, Layer] = {
    action match {
      case la:LayerAction => la.run(this)
    }
  }

  def updateMap(cell: WorldCell): Layer = {
    Layer(id, level, users, map.update(cell), facilities)
  }
}
