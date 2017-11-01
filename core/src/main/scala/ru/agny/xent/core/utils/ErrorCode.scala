package ru.agny.xent.core.utils

import ru.agny.xent.messages.Response

object ErrorCode extends Enumeration {
  val LAYER_NOT_FOUND,
  USER_NOT_FOUND,
  NOT_ENOUGH_LIFEPOWER,
  NOT_ENOUGH_RESOURCES,
  RESOURCE_CANT_BE_PRODUCED,
  RESOURCE_CANT_BE_CLAIMED,
  TROOP_CANT_BE_CREATED,
  BUILDING_NOT_FOUND,
  BUILDING_CANT_BE_PLACED,
  BUILDING_SCHEMA_NOT_EXIST,
  PLACE_NOT_FOUND
  = Value

  def get(code: Error): Response = ??? //TODO read from properties and build response

}
