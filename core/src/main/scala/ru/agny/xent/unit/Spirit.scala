package ru.agny.xent.unit

case class Spirit(var points: Int, base: Int, capacity: Int) {
  def change(x: Int) = {
    points = points + x
  }
}
