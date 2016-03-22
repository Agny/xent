package ru.agny.xent

sealed trait Cell
trait Placed
case class WorldCell(x: Int, y: Int, var content:List[Placed]) extends Cell
case class LocalCell() extends Cell
