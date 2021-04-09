package ru.agny.xent.realm

import ru.agny.xent._

case class Progress(
  private var value: TimeInterval,
  private var cap: TimeInterval
) {
  def fill(volume: TimeInterval): Boolean = {
    val x = value + volume
    if (x < cap) {
      value = x
      false
    } else {
      value = x - cap
      true
    }
  }

  def updateCap(v: TimeInterval): Unit = {
    cap = v
  }

  def isDone(): Boolean = cap == 0
}

object Progress {
  val DefaultCap = 100

  def Start() = Progress(0, DefaultCap)
}
