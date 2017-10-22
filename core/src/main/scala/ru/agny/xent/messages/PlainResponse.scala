package ru.agny.xent.messages

case class PlainResponse(value: String) extends Response[String]
object ResponseOk extends PlainResponse("Ok")
