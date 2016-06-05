package ru.agny.xent.utils

case class IdGen(i: Int) {
  private val ids =Iterator.from(i, 1)
  def next = ids.next()
}
