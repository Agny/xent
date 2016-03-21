package ru.agny.xent

sealed trait Cell
case class WorldCell() extends Cell
case class LocalCell() extends Cell
case class Pos(x: Int, y: Int, z: Int)
