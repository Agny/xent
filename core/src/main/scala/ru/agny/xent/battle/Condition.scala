package ru.agny.xent.battle

import ru.agny.xent.core.Progress.ProgressTime

trait Condition {
  def isMet(time: ProgressTime): Boolean
}
