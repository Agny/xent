package ru.agny.xent.core

import ru.agny.xent.core.inventory.Extractable

case class ResourceCell(c: Coordinate, resource: Extractable) extends Cell
