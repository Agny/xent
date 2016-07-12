package ru.agny.xent

import ru.agny.xent.core.WorldCell
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Seq[User], map: LayerMap, facilities: List[FacilityTemplate]) {

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

case class LayerMap(private val cells: Vector[Vector[WorldCell]]) {

  def find(c: WorldCell): Option[WorldCell] = {
    (c.x, c.y) match {
      case (x, y) if (x >= 0 && x < cells.length) && (y >= 0 && y < cells(x).length) => Some(cells(x)(y))
      case _ => None
    }
  }

  def update(c: WorldCell): LayerMap = {
    find(c) match {
      case Some(v) =>
        val yLayer = cells(v.x).updated(v.y, c)
        LayerMap(cells.updated(v.x, yLayer))
      case None => this
    }
  }

}
