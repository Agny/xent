package ru.agny.xent.core.utils

import java.util.concurrent.atomic.AtomicLong

object ItemIdGenerator {
  private val id = new AtomicLong(0)

  def next = id.incrementAndGet()

}
