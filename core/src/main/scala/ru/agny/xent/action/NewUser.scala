package ru.agny.xent.action

import ru.agny.xent.core.city.City
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.core.{Layer, User}
import ru.agny.xent.messages.Response

//TODO address city coordinates
case class NewUser(id: UserId, name: String) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = Right(layer.copy(users = User(id, name, City.empty(0, 0)) +: layer.users))
}
