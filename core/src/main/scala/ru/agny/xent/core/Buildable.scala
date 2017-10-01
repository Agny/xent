package ru.agny.xent.core

import ru.agny.xent.core.inventory.DelayableItem
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.utils.SelfAware

trait Buildable extends DelayableItem with SelfAware {
  val buildTime: ProgressTime
  override val yieldTime = buildTime
  val state: Facility.State
  def build: Self
  def finish: Self
}
