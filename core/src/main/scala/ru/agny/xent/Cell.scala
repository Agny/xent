package ru.agny.xent

sealed trait Cell[T <: Placed]
case class WorldCell() extends Cell[Global]
case class LocalCell() extends Cell[Local]
