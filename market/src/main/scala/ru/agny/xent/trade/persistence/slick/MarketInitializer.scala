package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.persistence.slick.CoreInitializer

import scala.concurrent.Await
import scala.concurrent.duration._

object MarketInitializer {

  val toInit = Seq(
    LotEntity.table,
    BidEntity.table,
  )

  def init() = {
    CoreInitializer.init()

    val creations = CoreInitializer.createIfNotExists(toInit)
    Await.result(creations, Duration.Inf)
    true
  }

}
