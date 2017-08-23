package ru.agny.xent.battle

import ru.agny.xent.core.unit.Occupation
import ru.agny.xent.core.unit.Speed.Distance

trait Step extends Occupation {
  def tick(distance: Distance): (Step, Distance)
  def isComplete: Boolean
}
