package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.persistence.slick.CoreInitializer

import scala.concurrent.Await
import scala.concurrent.duration._

class MarketInitializer(path: String) {

  val toInit = Seq(
    LotEntity.table,
    BidEntity.table,
  )
  val core = CoreInitializer.forConfig(path)

  def init() = {
    core.init()

    val creations = core.createIfNotExists(toInit)
    Await.result(creations, Duration.Inf)
    true
  }

}

object MarketInitializer {
  def forConfig(path: String): MarketInitializer = new MarketInitializer(path)
}
