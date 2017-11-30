package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.DefaultProfile.api._
import ru.agny.xent.persistence.slick.{ItemStackDB, UserDB}

object BidDB {
  private lazy val users = UserDB.table
  private lazy val stack = ItemStackDB.table
  lazy val table = TableQuery[BidTable]

  class BidTable(tag: Tag) extends Table[BidFlat](tag, "strict") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def itemStackId = column[Long]("stack_id")

    override def * = (userId, itemStackId).mapTo[BidFlat]

    def user = foreignKey("user_fk", userId, users)(_.id)

    def itemStack = foreignKey("stack_fk", itemStackId, stack)(_.id)
  }

  case class BidFlat(user: UserId, itemId: ItemId)

}
