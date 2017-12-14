package ru.agny.xent.persistence.slick

import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.UserEntity.UserFlat
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class UserRepository(configPath: String) extends ConfigurableRepository {

  private val users = UserEntity.table

  def create(name: String): Future[UserId] = {
    val insert = users returning users.map(_.id) += UserFlat(None, name, System.currentTimeMillis())
    val action = insert.cleanUp {
      case Some(e) => logger.error(e.getMessage); DBIO.failed(e)
      case None => DBIO.successful()
    }
    db.run(action)
  }

  def delete(id: UserId) = {
    val action = users.filter(_.id === id).delete
    db.run(action)
  }

}
