package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.WorldCell
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Seq[User], cells: List[WorldCell], facilities: List[FacilityTemplate]) {

  def tick(action: (Message, Action)): Layer = {
    val (acted, idle) = users.span(x => x.id == action._1.user)
    val updated = acted.map(x => x.work(action._1, action._2))
    Layer(id, level, idle ++ updated, cells, facilities)
  }

  def join(added: User): (Layer, User) = {
    (Layer(id, level, users :+ added, cells, facilities), added)
  }

  def leave(left: UserId): (Layer, User) = {
    tick((EmptyMessage(left), DoNothing))
    val user = users.find(x => x.id == left).get
    (Layer(id, level, users.diff(Seq(user)), cells, facilities), user)
  }
}
