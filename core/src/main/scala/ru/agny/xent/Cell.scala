package ru.agny.xent

sealed trait Cell
case class WorldCell(x: Int, y: Int, resource: Option[Extractable]) extends Cell
case class LocalCell() extends Cell
