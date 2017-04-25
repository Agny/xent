package ru.agny.xent.battle.unit

case class SpiritBar(points: Int, regen: Int, cap: Int) {
  def change(x: Int) = SpiritBar(points + x, regen, cap)
}
