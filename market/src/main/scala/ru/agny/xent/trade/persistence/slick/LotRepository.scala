package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.persistence.slick.DefaultProfile._
import ru.agny.xent.persistence.slick.DefaultProfile.api._
import ru.agny.xent.persistence.slick.ItemStackEntity.ItemStackFlat
import ru.agny.xent.persistence.slick.{ItemStackEntity, UserEntity}
import ru.agny.xent.trade._
import ru.agny.xent.trade.persistence.slick.BidEntity.BidFlat
import ru.agny.xent.trade.persistence.slick.LotEntity.LotFlat
import slick.jdbc.{ResultSetConcurrency, ResultSetType}

object LotRepository {
  def update(lot: Lot) = ???

  def create(lot: Lot) = ???

  def read(lot: ItemId): Lot = ???

  def delete(lot: ItemId) = ???


  private lazy val users = UserEntity.table
  private lazy val stack = ItemStackEntity.table
  private lazy val bid = BidEntity.table
  private lazy val lots = LotEntity.table

  def load(start: Int = 0, limit: Int = 20) = {
    val query = for {
      ((((flat, buyouts), items), bids), bidItems) <- lots.sortBy(_.until.desc).drop(start).take(limit).
        join(stack).on(_.buyoutId === _.id).
        join(stack).on(_._1.itemStackId === _.id).
        joinLeft(bid).
        joinLeft(stack).on { case ((((_, _), _), b), bitem) =>
        b.map(_.itemStackId === bitem.id)
      }
    } yield (flat, buyouts, items, bids, bidItems)
    db.stream(query.result.withStatementParameters(
      rsType = ResultSetType.ForwardOnly,
      rsConcurrency = ResultSetConcurrency.ReadOnly,
      fetchSize = limit).transactionally)
      .mapResult {
        case (lotFlat, buyout, item, bidFlat, bidItem) => mapToLot(lotFlat, buyout.toItemStack, item.toItemStack, bidFlat, bidItem)
      }
  }

  private def mapToLot(lotFlat: LotFlat, buyout: ItemStack, item: ItemStack, bidFlat: Option[BidFlat], bidItem: Option[ItemStackFlat]): Lot = {
    lotFlat.tpe.v match {
      case dealer if dealer == Dealer.toString() => Dealer(lotFlat.id, lotFlat.user, item, Price(buyout), lotFlat.until)
      case strict if strict == Strict.toString() => Strict(lotFlat.id, lotFlat.user, item, Price(buyout), lotFlat.until)
      case nStrict if nStrict == NonStrict.toString() =>
        val mbBid = for {
          b <- bidFlat
          bItem <- bidItem
        } yield Bid(b.user, Price(bItem.toItemStack))
        NonStrict(lotFlat.id, lotFlat.user, item, Price(buyout), lotFlat.until, mbBid)
    }
  }

}
