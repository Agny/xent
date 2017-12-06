package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.DefaultProfile.api._
import ru.agny.xent.persistence.slick.{ItemStackEntity, UserEntity}

object BidEntity {
  private val users = UserEntity.table
  private val stack = ItemStackEntity.table
  private val lots = LotEntity.table
  val table = TableQuery[BidTable]

  class BidTable(tag: Tag) extends Table[BidFlat](tag, "bid") {
    def lotId = column[Long]("lot_id", O.PrimaryKey)

    def userId = column[Long]("user_id")

    def itemStackId = column[Long]("stack_id")

    override def * = (lotId.?, userId, itemStackId).mapTo[BidFlat]

    def lot = foreignKey("lot_fk", lotId, lots)(_.id)

    def user = foreignKey("user_fk", userId, users)(_.id)

    def itemStack = foreignKey("stack_fk", itemStackId, stack)(_.id)
  }

  case class BidFlat(lotId: Option[Long], user: UserId, itemId: ItemId)

}
