package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.persistence.slick.ConfigurableRepository
import slick.jdbc.PostgresProfile.api._

case class BidRepository(configPath: String) extends ConfigurableRepository {

  private val bids = BidEntity.table
}
