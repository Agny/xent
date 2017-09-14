package ru.agny.xent.battle

import ru.agny.xent.core.inventory.Progress.ProgressTime

trait Condition {
  def isMet(time: ProgressTime): Boolean
}
