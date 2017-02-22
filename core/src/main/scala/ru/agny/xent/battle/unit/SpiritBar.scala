package ru.agny.xent.battle.unit

case class SpiritBar(points: Int, regen: Int) {
  def change(x:Int) = SpiritBar(points + x, regen)
}
