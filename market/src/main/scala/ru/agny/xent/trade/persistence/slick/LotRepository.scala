package ru.agny.xent.trade.persistence.slick

import org.reactivestreams.Publisher
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.ItemStackEntity.ItemStackFlat
import ru.agny.xent.persistence.slick.{ConfigurableRepository, ItemStackEntity, UserEntity}
import ru.agny.xent.trade.Lot.LotId
import ru.agny.xent.trade._
import ru.agny.xent.trade.persistence.slick.BidEntity.BidFlat
import ru.agny.xent.trade.persistence.slick.LotEntity.{LotFlat, LotTable}
import ru.agny.xent.trade.persistence.slick.ReservedItemEntity.ReservedFlat

import scala.collection.immutable
import scala.concurrent.Future
import slick.jdbc.{ResultSetConcurrency, ResultSetType}
import slick.jdbc.PostgresProfile.api._

case class LotRepository(configPath: String) extends ConfigurableRepository {
  private val users = UserEntity.table
  private val stack = ItemStackEntity.table
  private val bids = BidEntity.table
  private val lots = LotEntity.table
  private val reserved = ReservedItemEntity.table

  def load(start: Int = 0, limit: Int = 20): Publisher[Lot] = {
    val query = fullLoad(lots.sortBy(_.until.desc).drop(start).take(limit))
    db.stream(query.result.withStatementParameters(
      rsType = ResultSetType.ForwardOnly,
      rsConcurrency = ResultSetConcurrency.ReadOnly,
      fetchSize = limit).transactionally)
      .mapResult(mapToLot)
  }

  def create(lot: PlaceLot): Future[LotId] = {
    val minPriceAction = lot.tpe match {
      case NonStrict.`type` => lot.minPrice.map(
        x => stack returning stack.map(_.id) += ItemStackFlat(None, x.stackValue, x.id, x.singleWeight)
      )
      case _ => None
    }
    val action = insertLotAction(lot, minPriceAction)
    db.run(action.transactionally)
  }

  def read(lot: Long): Future[Option[Lot]] = {
    val query = fullLoad(lots.filter(_.id === lot))
    db.run(query.result.headOption).map {
      case Some(x) => Some(mapToLot(x))
      case _ => None
    }
  }

  def findByUser(id: UserId): Future[Seq[Lot]] = {
    val query = fullLoad(lots.filter(_.userId === id))
    db.run(query.result).map {
      case lots@_ +: _ => lots.map(mapToLot)
      case _ => immutable.Seq.empty
    }
  }

  def updateBid(lot: LotId, bid: Bid): Future[Boolean] = {
    val isGreaterThanMinPrice = lots.filter(l => l.id === lot && l.userId =!= bid.owner)
      .join(stack).on((l, s) => l.minPriceId === s.id && s.stackValue <= bid.price.stackValue).exists
    val isGreaterThanPrevBid = bids.filter(b => b.lotId === lot && b.userId =!= bid.owner)
      .joinLeft(stack).on((b, s) => b.itemStackId === s.id && s.stackValue < bid.price.stackValue).map(_._2.nonEmpty)

    val resultAction = isGreaterThanPrevBid.result.headOption.flatMap {
      case Some(true) => insertBidAction(lot, bid)
      case None => isGreaterThanMinPrice.result.flatMap {
        case true => insertBidAction(lot, bid)
        case false => DBIO.failed(new IllegalStateException(s"$bid price is less than minimal price of Lot[$lot]"))
      }
      case _ => DBIO.failed(new IllegalStateException(s"$bid price is less than previous bid"))
    }
    db.run(resultAction.transactionally).map {
      case rowsAffected@0 => false
      case 1 => true
    }
  }

  def revertBid(lot: LotId, toRevert: Bid, prevBid: Option[Bid]): Future[Boolean] = {
    val itemToRevert = toRevert.price
    val selectedBid = bids.filter(b => b.lotId === lot)
    val isNotChanged = selectedBid.joinLeft(stack).on((b, s) => b.itemStackId === s.id
      && s.stackValue === itemToRevert.stackValue
      && s.itemId === itemToRevert.id
    ).exists
    val resultAction = isNotChanged.result.flatMap {
      case true => prevBid match {
        case Some(v) => selectedBid.delete >> insertBidAction(lot, v)
        case None => selectedBid.delete
      }
      case false => DBIO.failed(new IllegalStateException(s"Bid $toRevert has been overwritten already"))
    }

    db.run(resultAction.transactionally).map {
      case rowsAffected@0 => false
      case 1 => true
    }
  }

  def buy(lot: LotId, withBid: Bid): Future[Lot] = {
    val paidItem = withBid.price
    val retrieveLot = lots.filter(b => b.id === lot && b.userId =!= withBid.owner)
    val lotWithItem = retrieveLot.join(stack).on((l, s) => l.buyoutId === s.id && s.stackValue === paidItem.stackValue)
    val priceValidated = lotWithItem.exists

    val resultAction = priceValidated.result.flatMap {
      case true => fullLoad(retrieveLot).result.head
      case _ => DBIO.failed(new IllegalStateException(s"Bid $withBid can't be applied to Lot[$lot]"))
    }
    db.run(resultAction.transactionally).map(loaded => mapToLot(loaded))
  }

  def reserveItem(forUser: UserId, item: ItemStack): Future[Boolean] = {
    val idReturn = stack returning stack.map(_.id)
    val action = for {
      itemId <- idReturn += ItemStackFlat(None, item.stackValue, item.id, item.singleWeight)
      x <- reserved += ReservedFlat(forUser, itemId)
    } yield x
    db.run(action.transactionally).map {
      case rowsAffected@0 => false
      case 1 => true
    }
  }

  def delete(lot: LotId): Future[Boolean] = {
    db.run(lots.filter(_.id === lot).delete).map {
      case rowsAffected@0 => false
      case 1 => true
    }
  }

  private def insertLotAction(lot: PlaceLot, withMinPrice: Option[DBIOAction[ItemId, NoStream, Effect.Write]] = None) = {
    val idReturn = stack returning stack.map(_.id)
    val lotIdReturn = lots returning lots.map(_.id)
    val (item, priceItem) = (lot.item, lot.buyout)
    for {
      itemId <- idReturn += ItemStackFlat(None, item.stackValue, item.id, item.singleWeight)
      priceId <- idReturn += ItemStackFlat(None, priceItem.stackValue, priceItem.id, priceItem.singleWeight)
      minPriceId <- withMinPrice getOrElse DBIO.successful(priceId)
      x <- lotIdReturn += LotFlat(None, lot.user, itemId, priceId, minPriceId, lot.until, lot.tpe)
    } yield x
  }

  private def insertBidAction(lot: LotId, bid: Bid) = {
    val itemToAdd = bid.price
    val insertItemStack = stack.returning(stack.map(_.id))
      .+=(ItemStackFlat(None, itemToAdd.stackValue, itemToAdd.id, itemToAdd.singleWeight))

    for {
      s <- insertItemStack
      b <- bids.insertOrUpdate(BidFlat(Some(lot), bid.owner, s))
    } yield b
  }

  private def fullLoad(origin: Query[LotTable, LotTable#TableElementType, Seq]) = {
    val query = origin.
      join(stack).on(_.buyoutId === _.id).
      join(stack).on(_._1.minPriceId === _.id).
      join(stack).on(_._1._1.itemStackId === _.id).
      joinLeft(bids).
      joinLeft(stack).on {
      case (((((_, _), _), _), b), bitem) =>
        b.map(_.itemStackId === bitem.id)
    }
    for {
      (((((flat, buyouts), items), minPrice), bids), bidItems) <- query
    } yield (flat, buyouts, items, minPrice, bids, bidItems)

  }

  private def mapToLot(params: (LotFlat, ItemStackFlat, ItemStackFlat, ItemStackFlat, Option[BidFlat], Option[ItemStackFlat])): Lot = params match {
    case (lotFlat, buyout, minPrice, item, bidFlat, bidItem) =>
      lotFlat.tpe match {
        case dealer if dealer == Dealer.`type` => Dealer(lotFlat.id.get, lotFlat.user, item.toItemStack, buyout.toItemStack, lotFlat.until)
        case strict if strict == Strict.`type` => Strict(lotFlat.id.get, lotFlat.user, item.toItemStack, buyout.toItemStack, lotFlat.until)
        case nStrict if nStrict == NonStrict.`type` =>
          val mbBid = for {
            b <- bidFlat
            bItem <- bidItem
          } yield Bid(b.user, bItem.toItemStack)
          NonStrict(lotFlat.id.get, lotFlat.user, item.toItemStack, buyout.toItemStack, minPrice.toItemStack, lotFlat.until, mbBid)
      }
  }

}
