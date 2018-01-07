package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.ConfigurableRepository
import ru.agny.xent.trade.persistence.slick.ReservedItemEntity.ReservedFlat

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

case class ReservedItemRepository(configPath: String) extends ConfigurableRepository {

  private val reserved = ReservedItemEntity.table

  def findByUser(user: UserId): Future[Seq[ReservedFlat]] = {
    val query = reserved.filter(_.userId === user)
    db.run(query.result)
  }

  def clean() = db.run(reserved.delete)

}
