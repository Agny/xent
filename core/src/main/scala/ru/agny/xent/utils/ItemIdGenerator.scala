package ru.agny.xent.utils

import java.util.concurrent.atomic.AtomicLong

import ru.agny.xent.ItemId

object ItemIdGenerator {
  private val id = new AtomicLong(0)

  def next: ItemId = id.incrementAndGet()

}
