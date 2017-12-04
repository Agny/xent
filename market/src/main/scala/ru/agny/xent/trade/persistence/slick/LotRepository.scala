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

import scala.concurrent.ExecutionContext.Implicits._
import slick.jdbc.{ResultSetConcurrency, ResultSetType}

object LotRepository {
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

  def create(lot: Lot) = {
    val idReturn = stack returning stack.map(_.id)
    val lotReturn = lots returning lots.map(_.id) into { case (l, id) => l.copy(id = Some(id)) }
    val (item, priceItem) = (lot.item, lot.buyout.amount)
    val query = for {
      itemId <- idReturn += ItemStackFlat(None, item.stackValue, item.id, item.singleWeight)
      priceId <- idReturn += ItemStackFlat(None, priceItem.stackValue, priceItem.id, priceItem.singleWeight)
      x <- lotReturn += LotFlat(None, lot.user, itemId, priceId, lot.until, None, lot.tpe)
    } yield x
    db.run(query.transactionally)
  }

  def read(lot: ItemId): Lot = ???

  def update(lot: Lot) = ???

  def delete(lot: ItemId) = ???

  private def mapToLot(lotFlat: LotFlat, buyout: ItemStack, item: ItemStack, bidFlat: Option[BidFlat], bidItem: Option[ItemStackFlat]): Lot = {
    lotFlat.tpe.v match {
      case dealer if dealer == Dealer.toString() => Dealer(lotFlat.id.get, lotFlat.user, item, Price(buyout), lotFlat.until)
      case strict if strict == Strict.toString() => Strict(lotFlat.id.get, lotFlat.user, item, Price(buyout), lotFlat.until)
      case nStrict if nStrict == NonStrict.toString() =>
        val mbBid = for {
          b <- bidFlat
          bItem <- bidItem
        } yield Bid(b.user, Price(bItem.toItemStack))
        NonStrict(lotFlat.id.get, lotFlat.user, item, Price(buyout), lotFlat.until, mbBid)
    }
  }

}
