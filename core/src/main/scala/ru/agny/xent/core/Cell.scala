package ru.agny.xent.core

import ru.agny.xent.UserType.UserId

sealed trait Cell
case class WorldCell(x: Int, y: Int, resource: Option[Extractable] = None, owner: Option[UserId] = None) extends Cell
case class LocalCell() extends Cell
