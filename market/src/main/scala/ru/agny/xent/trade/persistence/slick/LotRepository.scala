package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.persistence.slick.ItemStackEntity.ItemStackFlat
import ru.agny.xent.persistence.slick.{ItemStackEntity, ConfigurableRepository, UserEntity}
import ru.agny.xent.trade._
import ru.agny.xent.trade.persistence.slick.BidEntity.BidFlat
import ru.agny.xent.trade.persistence.slick.LotEntity.{LotFlat, LotTable}

import scala.concurrent.ExecutionContext.Implicits._
import slick.jdbc.{ResultSetConcurrency, ResultSetType}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

case class LotRepository(configPath: String) extends ConfigurableRepository {
  private val users = UserEntity.table
  private val stack = ItemStackEntity.table
  private val bids = BidEntity.table
  private val lots = LotEntity.table

  def load(start: Int = 0, limit: Int = 20) = {
    val query = for {
      ((((flat, buyouts), items), bids), bidItems) <- fullLoad(lots.sortBy(_.until.desc).drop(start).take(limit))
    } yield (flat, buyouts, items, bids, bidItems)
    db.stream(query.result.withStatementParameters(
      rsType = ResultSetType.ForwardOnly,
      rsConcurrency = ResultSetConcurrency.ReadOnly,
      fetchSize = limit).transactionally)
      .mapResult {
        case (lotFlat, buyout, item, bidFlat, bidItem) => mapToLot(lotFlat, buyout.toItemStack, item.toItemStack, bidFlat, bidItem)
      }
  }

  def create(lot: PlaceLot) = {
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

  def read(lot: Long): Future[Option[Lot]] = {
    val query = for {
      ((((flat, buyouts), items), bids), bidItems) <- fullLoad(lots.filter(_.id === lot))
    } yield (flat, buyouts, items, bids, bidItems)
    db.run(query.result.headOption).map {
      case Some((lotFlat, buyout, item, bidFlat, bidItem)) => Some(mapToLot(lotFlat, buyout.toItemStack, item.toItemStack, bidFlat, bidItem))
      case _ => None
    }
  }

  def updateBid(lot: Long, bid: Bid) = {
    val itemToAdd = bid.price.amount

    val isUpdateValid = bids.filter(_.lotId === lot)
      .joinLeft(stack).on((b, s) => b.itemStackId === s.id && s.stackValue < itemToAdd.stackValue).map(_._2.nonEmpty)

    val insertItemStack = stack.returning(stack.map(_.id))
      .+=(ItemStackFlat(None, itemToAdd.stackValue, itemToAdd.id, itemToAdd.singleWeight))

    val updateAction = for {
      s <- insertItemStack
      _ <- bids.insertOrUpdate(BidFlat(Some(lot), bid.owner, s))
      l <- lots.map(_.lastBidId).update(Some(lot))
    } yield l

    val resultAction = isUpdateValid.result.headOption.flatMap {
      case Some(true) | None => updateAction
      case _ => DBIO.failed(new IllegalArgumentException("???")) // TODO error handle
    }
    db.run(resultAction.transactionally)
  }

  def buy(lot: Long, withBid: Bid) = {
    val paidItem = withBid.price.amount
    val retrieveLot = lots.filter(_.id === lot)

    val priceValidated = retrieveLot.join(stack).on((l, s) => l.buyoutId === s.id && s.stackValue === paidItem.stackValue).exists

    val resultAction = priceValidated.result.flatMap {
      case true => retrieveLot.delete
      case _ => DBIO.failed(new IllegalArgumentException("???"))
    }
    db.run(resultAction.transactionally)
  }

  private def fullLoad(origin: Query[LotTable, LotTable#TableElementType, Seq]) = {
    origin.
      join(stack).on(_.buyoutId === _.id).
      join(stack).on(_._1.itemStackId === _.id).
      joinLeft(bids).
      joinLeft(stack).on {
      case ((((_, _), _), b), bitem) =>
        b.map(_.itemStackId === bitem.id)
    }
  }

  private def mapToLot(lotFlat: LotFlat, buyout: ItemStack, item: ItemStack, bidFlat: Option[BidFlat], bidItem: Option[ItemStackFlat]): Lot = {
    lotFlat.tpe match {
      case dealer if dealer == Dealer.`type` => Dealer(lotFlat.id.get, lotFlat.user, item, Price(buyout), lotFlat.until)
      case strict if strict == Strict.`type` => Strict(lotFlat.id.get, lotFlat.user, item, Price(buyout), lotFlat.until)
      case nStrict if nStrict == NonStrict.`type` =>
        val mbBid = for {
          b <- bidFlat
          bItem <- bidItem
        } yield Bid(b.user, Price(bItem.toItemStack))
        NonStrict(lotFlat.id.get, lotFlat.user, item, Price(buyout), lotFlat.until, mbBid)
    }
  }

}
