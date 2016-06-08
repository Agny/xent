package ru.agny.xent

import ru.agny.xent.core.WorldCell
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Seq[User], cells: List[WorldCell], facilities: List[FacilityTemplate]) {

  def tick(actions: Map[Long, Action]): Layer = {
    val (acted, idle) = users.span(x => actions.contains(x.id))
    val updated = acted.map(x => x.work(actions(x.id)))

    val updatedResources = updated.flatMap(x => x.storage.resources)
    println(updatedResources + " -- " + users)

    Layer(id, level, idle ++ updated, cells, facilities)
  }

  def join(added: Seq[User]): Layer = {
    Layer(id, level, users ++ added, cells, facilities)
  }
}
