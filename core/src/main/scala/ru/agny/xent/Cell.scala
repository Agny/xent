package ru.agny.xent

sealed trait Cell
case class WorldCell() extends Cell
case class LocalCell() extends Cell
