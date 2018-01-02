package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.{ItemStackEntity, UserEntity}
import slick.jdbc.PostgresProfile.api._

object ReservedItemEntity {

  private lazy val users = UserEntity.table
  private lazy val stack = ItemStackEntity.table
  lazy val table = TableQuery[ReservedItemTable]

  class ReservedItemTable(tag: Tag) extends Table[ReservedFlat](tag, "reserved_item") {

    def userId = column[UserId]("user_id")

    def itemStackId = column[Long]("stack_id")

    override def * = (userId, itemStackId).mapTo[ReservedFlat]

    def user = foreignKey("user_fk", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def itemStack = foreignKey("stack_fk", itemStackId, stack)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  case class ReservedFlat(user: UserId, item: Long)

}
