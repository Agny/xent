package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.persistence.slick.CoreInitializer

import scala.concurrent.Await
import scala.concurrent.duration._

class MarketInitializer(configPath: String) {

  val toInit = Seq(
    LotEntity.table,
    BidEntity.table,
  )
  val core = CoreInitializer.forConfig(configPath)

  def init() = {
    core.init()

    val creations = core.createIfNotExists(toInit)
    Await.result(creations, Duration.Inf)
    true
  }

}

object MarketInitializer {
  lazy val common = new MarketInitializer("db")
  lazy val test = new MarketInitializer("db-test")

  def forConfig(path: String): MarketInitializer = path match {
    case "db" => common
    case "db-test" => test
  }
}
