package ru.agny.xent.web

sealed trait WSMessage
case class IncomeMessage(tpe: String, body: String) extends WSMessage
case class MapView(objects: Vector[ObjectView]) extends WSMessage
case class ViewCenter(x: Int, y: Int, size: Int) extends WSMessage


