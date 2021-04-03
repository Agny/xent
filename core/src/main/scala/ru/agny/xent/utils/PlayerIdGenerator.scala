package ru.agny.xent.utils

import java.util.concurrent.atomic.AtomicLong

import ru.agny.xent.PlayerId

object PlayerIdGenerator {
  private val id = new AtomicLong(0)

  def next: PlayerId = id.incrementAndGet()

}
