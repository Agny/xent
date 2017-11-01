package ru.agny.xent.messages

trait Response {
  type T
  val value: T
}
