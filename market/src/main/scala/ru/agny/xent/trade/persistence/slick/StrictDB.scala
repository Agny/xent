package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.DefaultProfile.api._
import ru.agny.xent.persistence.slick.{ItemStackDB, UserDB}

object StrictDB {
  private lazy val users = UserDB.table
  private lazy val stack = ItemStackDB.table
  lazy val table = TableQuery[StrictTable]

  class StrictTable(tag: Tag) extends Table[StrictFlat](tag, "strict") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def until = column[Long]("until")

    def itemStackId = column[Long]("stack_id")

    def buyoutId = column[Long]("buyout_id")

    override def * = (id, userId, itemStackId, buyoutId, until).mapTo[StrictFlat]

    def user = foreignKey("user_fk", userId, users)(_.id)

    def itemStack = foreignKey("stack_fk", itemStackId, stack)(_.id)

    def buyout = foreignKey("buyout_fk", buyoutId, stack)(_.id)
  }

  case class StrictFlat(id: Long, user: UserId, itemId: ItemId, priceId: ItemId, until: TimeStamp)

}
