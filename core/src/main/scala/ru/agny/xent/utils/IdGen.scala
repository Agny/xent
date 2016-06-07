package ru.agny.xent.utils

case class IdGen(i: Int = 1) {
  private val ids = Iterator.from(i, 1)
  def next = ids.next()
}
