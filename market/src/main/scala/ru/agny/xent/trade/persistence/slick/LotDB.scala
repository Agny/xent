package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.DefaultProfile.api._
import ru.agny.xent.persistence.slick.{ItemStackDB, UserDB}
import ru.agny.xent.trade.LotType

object LotDB {

  import CustomColumnTypes._

  private lazy val users = UserDB.table
  private lazy val stack = ItemStackDB.table
  private lazy val bid = BidDB.table
  lazy val table = TableQuery[LotTable]

  class LotTable(tag: Tag) extends Table[LotFlat](tag, "lot") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def itemStackId = column[Long]("stack_id")

    def buyoutId = column[Long]("buyout_id")

    def until = column[Long]("until")

    def lastBidId = column[Option[Long]]("last_bid")

    def tpe = column[LotType]("type")

    override def * = (id, userId, itemStackId, buyoutId, until, lastBidId, tpe).mapTo[LotFlat]

    def user = foreignKey("user_fk", userId, users)(_.id)

    def itemStack = foreignKey("stack_fk", itemStackId, stack)(_.id)

    def buyout = foreignKey("buyout_fk", buyoutId, stack)(_.id)

    def lastBid = foreignKey("bid_fk", lastBidId, bid)(_.lotId)
  }

  case class LotFlat(id: Long, user: UserId, itemId: ItemId, priceId: ItemId, until: TimeStamp, lastBid: Option[Long], tpe: LotType)

}
