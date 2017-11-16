package ru.agny.xent.trade

import ru.agny.xent.core.Layer.LayerId

case class Board(layer: LayerId, lots: Map[Long, Lot]) {

  def update(v: Lot) = copy(lots = lots + (v.id -> v))

}
