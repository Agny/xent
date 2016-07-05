package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.WorldCell
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Seq[User], map: LayerMap, facilities: List[FacilityTemplate]) {

  def tick(action: (Message, Action)): Layer = {
    val (acted, idle) = users.span(x => x.id == action._1.user)
    val updated = acted.map(x => x.work(action._1, action._2))
    Layer(id, level, idle ++ updated, map, facilities)
  }

  def join(added: User): (Layer, User) = {
    (Layer(id, level, users :+ added, map, facilities), added)
  }

  def leave(left: UserId): (Layer, User) = {
    tick((EmptyMessage(left), DoNothing))
    val user = users.find(x => x.id == left).get
    (Layer(id, level, users.diff(Seq(user)), map, facilities), user)
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
