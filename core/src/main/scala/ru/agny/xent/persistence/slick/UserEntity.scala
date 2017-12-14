package ru.agny.xent.persistence.slick

import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.utils.UserType.UserId
import slick.jdbc.PostgresProfile.api._

object UserEntity {
  val table = TableQuery[UserTable]

  class UserTable(tag: Tag) extends Table[UserFlat](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def lastAction = column[ProgressTime]("last_action")

    override def * = (id.?, name, lastAction).mapTo[UserFlat]
  }

  case class UserFlat(id: Option[UserId], name: String, lastAction: Long)

}
