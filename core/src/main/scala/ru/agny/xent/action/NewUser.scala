package ru.agny.xent.action

import ru.agny.xent.core.city.City
import ru.agny.xent.core.utils.LayerGenerator
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.core.{Layer, User}
import ru.agny.xent.messages.Response

case class NewUser(id: UserId, name: String) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = {
    LayerGenerator.newCityCoordinate(layer) match {
      case Some(v) =>
        val city = City.empty(v.x, v.y)
        val user = User(id, name, city)
        Right(layer.copy(users = user +: layer.users)) //, armies = layer.armies.add(city)))
      case None => Left(Response(s"There is no place for new city in this layer ${layer.id}"))
    }
  }
}
