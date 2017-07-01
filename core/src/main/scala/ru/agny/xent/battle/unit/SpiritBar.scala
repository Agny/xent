package ru.agny.xent.battle.unit

case class SpiritBar(points: Int, regen: Int, capacity: Int) {
  def change(x: Int) = SpiritBar(points + x, regen, capacity)
}
