package ru.agny.xent.battle

import ru.agny.xent.core.unit.Occupation

trait Step extends Occupation {
  def isComplete: Boolean
}
