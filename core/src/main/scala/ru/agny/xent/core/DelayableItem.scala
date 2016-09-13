package ru.agny.xent.core

import ru.agny.xent.core.Progress.ProductionTime

trait DelayableItem {
  val name: String
  val yieldTime: ProductionTime
}

object Progress {
  type ProductionTime = Long
}
