package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.ConfigurableRepository
import ru.agny.xent.trade.persistence.slick.ReservedItemEntity.ReservedFlat

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{ResultSetConcurrency, ResultSetType}

case class ReservedItemRepository(configPath: String) extends ConfigurableRepository {

  private val reserved = ReservedItemEntity.table

  def load(start: Int = 0, limit: Int = 20): Future[Seq[ReservedFlat]] = {
    val query = reserved.drop(start).take(limit)
    db.run(query.result.withStatementParameters(
      rsType = ResultSetType.ForwardOnly,
      rsConcurrency = ResultSetConcurrency.ReadOnly,
      fetchSize = limit).transactionally)
  }

  def findByUser(user: UserId): Future[Seq[ReservedFlat]] = {
    val query = reserved.filter(_.userId === user)
    db.run(query.result)
  }
}
