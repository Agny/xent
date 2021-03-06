package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.{ItemStackEntity, UserEntity}
import ru.agny.xent.trade.LotType
import slick.jdbc.PostgresProfile.api._

object LotEntity {

  import CustomColumnTypes._

  private lazy val users = UserEntity.table
  private lazy val stack = ItemStackEntity.table
  lazy val table = TableQuery[LotTable]

  class LotTable(tag: Tag) extends Table[LotFlat](tag, "lot") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def itemStackId = column[Long]("stack_id")

    def buyoutId = column[Long]("buyout_id")

    def minPriceId = column[Long]("min_price_id")

    def until = column[Long]("until")

    def tpe = column[LotType]("type")

    override def * = (id.?, userId, itemStackId, buyoutId, minPriceId, until, tpe).mapTo[LotFlat]

    def user = foreignKey("user_fk", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def itemStack = foreignKey("stack_fk", itemStackId, stack)(_.id, onDelete = ForeignKeyAction.Cascade)

    def buyout = foreignKey("buyout_fk", buyoutId, stack)(_.id, onDelete = ForeignKeyAction.Cascade)

    def minPrice = foreignKey("min_price_fk", minPriceId, stack)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  case class LotFlat(id: Option[Long], user: UserId, itemId: ItemId, priceId: ItemId, minPriceId: ItemId, until: TimeStamp, tpe: LotType)

}
