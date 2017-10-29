package ru.agny.xent.action

import ru.agny.xent.battle.CityProxy
import ru.agny.xent.core.city.City
import ru.agny.xent.core.utils.LayerGenerator
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.core.{Layer, User}
import ru.agny.xent.messages.PlainResponse

case class NewUser(id: UserId, name: String) extends LayerAction {
  override def run(layer: Layer): Either[PlainResponse, Layer] = {
    LayerGenerator.newCityCoordinate(layer) match {
      case Some(v) =>
        val city = City.empty(v.x, v.y)
        val proxy = CityProxy(id, layer.id, city.storage.resources, Vector.empty, city.c)
        val user = User(id, name, city)
        Right(layer.copy(users = user +: layer.users, armies = layer.armies.add(proxy)))
      case None => Left(PlainResponse(s"There is no place for new city in this layer ${layer.id}"))
    }
  }
}
