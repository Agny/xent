package ru.agny.xent.messages

trait Response[T] {
  val value: T
}
