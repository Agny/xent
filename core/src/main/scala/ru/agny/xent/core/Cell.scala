package ru.agny.xent.core

import ru.agny.xent.User

sealed trait Cell
case class WorldCell(x: Int, y: Int, resource: Option[Extractable] = None, owner: Option[User] = None) extends Cell
case class LocalCell() extends Cell
