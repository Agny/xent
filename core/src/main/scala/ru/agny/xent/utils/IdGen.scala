package ru.agny.xent.utils

case class IdGen() {
  var i = 0

  def next: Int = {
    i = i + 1
    i
  }
}
