package ru.agny.xent.web

sealed trait WSMessage
case class IncomeMessage(tpe: String, body: String) extends WSMessage