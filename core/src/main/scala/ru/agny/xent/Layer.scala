package ru.agny.xent

import ru.agny.xent.core.WorldCell
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Seq[User], cells: List[WorldCell], facilities: List[FacilityTemplate]) {

  def tick(actions: List[(ActionResult, Action)]): Layer = {
    val (acted, idle) = users.span(x => actions.exists(p => p._1.id == x.id))
    val userActions = acted.map(x => x -> actions.filter(p => x.id == p._1.id))
    val updated = userActions.flatMap(x => x._2.map(y => x._1.work(y._1, y._2)))
    Layer(id, level, idle ++ updated, cells, facilities)
  }

  def join(added: Seq[User]): Layer = {
    Layer(id, level, users ++ added, cells, facilities)
  }
}
