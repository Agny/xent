package ru.agny.xent.web

trait WSMessage
case class IncomeMessage(tpe: String, body: String) extends WSMessage