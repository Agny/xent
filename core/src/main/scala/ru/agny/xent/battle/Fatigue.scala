package ru.agny.xent.battle

case class Fatigue(v: Int) {
  def ++ = Fatigue(v + 1)
}

object Fatigue {
  val MAX = Fatigue(Int.MaxValue)

  implicit def toInt(f: Fatigue): Int = f.v
}
