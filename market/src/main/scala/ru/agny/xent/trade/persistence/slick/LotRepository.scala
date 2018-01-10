package ru.agny.xent.trade.persistence.slick

import org.reactivestreams.Publisher
import ru.agny.xent.core.inventory.Item.ItemId
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
        x => stack returning stack.map(_.id) += ItemStackFlat(None, x.amount, x.id)
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

  def biddingPreparement(lotId: LotId, bid: Bid): Future[Option[Bid]] = {
    val lot = fullLoad(lots.filter(_.id === lotId))

    val resultAction = lot.result.headOption.flatMap {
      case None => DBIO.failed(new IllegalStateException(s"Lot[$lotId] doesn't exist"))

      case invalidLotType@Some((lotFlat, _, _, _, _, _)) if lotFlat.tpe != NonStrict.`type` =>
        DBIO.failed(new IllegalStateException(s"lot $lotFlat is not biddable"))

      case sameUserLot@Some((lotFlat, _, _, _, _, _)) if lotFlat.user == bid.owner =>
        DBIO.failed(new IllegalStateException(s"User[${bid.owner}] can't bid his own Lot[$lot]"))

      case sameUserBid@Some((_, _, _, _, Some(bidFlat), _)) if bidFlat.user == bid.owner =>
        DBIO.failed(new IllegalStateException(s"User[${bid.owner}] can't bid over his own Bid[$bidFlat]"))

      case bidLessThanPrice@Some((_, _, minPrice, _, None, _)) if bid.price.amount < minPrice.stackValue =>
        DBIO.failed(new IllegalStateException(s"${bid.price} price is less than minimal price $minPrice"))

      case bidLessThanCurrentBid@Some((_, _, _, _, _, Some(currentPrice))) if bid.price.amount <= currentPrice.stackValue =>
        DBIO.failed(new IllegalStateException(s"${bid.price} is less than previous price $currentPrice"))

      case Some((_, _, _, _, Some(bidFlat), Some(bidItem))) =>
        bids.filter(_.lotId === bidFlat.lotId).delete >>
          stack.filter(_.id === bidItem.id).delete >>
          DBIO.successful(Some(Bid(bidFlat.user, bidItem)))

      case Some((_, _, _, _, None, _)) => DBIO.successful(None)

      case x => DBIO.failed(new IllegalStateException(s"Unexpected input $x"))

    }

    db.run(resultAction.transactionally)
  }

  def insertBid(lot: LotId, bid: Bid): Future[Boolean] = {
    db.run(insertBidAction(lot, bid).transactionally).map {
      case rowsAffected@0 => false
      case 1 => true
    }
  }

  def revertBid(lot: LotId, prevBid: Option[Bid]): Future[Boolean] = {
    val selectedBid = bids.filter(b => b.lotId === lot)
    val resultAction = selectedBid.result.headOption.flatMap {
      case Some(_) => DBIO.failed(new IllegalStateException(s"Lot $lot has been bidded already"))
      case None => selectedBid.delete >> (prevBid.map(v => insertBidAction(lot, v)) getOrElse DBIO.successful(1))
    }

    db.run(resultAction.transactionally).map {
      case rowsAffected@0 => false
      case 1 => true
    }
  }

  def buyPreparement(lot: LotId, buyer: UserId): Future[Lot] = {
    val retrieveLot = lots.filter(b => b.id === lot && b.userId =!= buyer)
    val lotWithPrice = retrieveLot.join(stack).on((l, s) => l.buyoutId === s.id)
    val priceValidated = lotWithPrice.exists

    val resultAction = priceValidated.result.flatMap {
      case true => fullLoad(retrieveLot).result.headOption
      case _ => DBIO.failed(new IllegalStateException(s"User[$buyer] can't buy his own Lot[$lot]"))
    }
    db.run(resultAction.transactionally).flatMap {
      case Some(v) => Future.successful(mapToLot(v))
      case None => Future.failed(new IllegalStateException(s"Lot[$lot] is already deleted"))
    }
  }

  def reserveItem(forUser: UserId, item: ItemHolder): Future[Boolean] = {
    val idReturn = stack returning stack.map(_.id)
    val action = for {
      itemId <- idReturn += ItemStackFlat(None, item.amount, item.id)
      x <- reserved += ReservedFlat(forUser, itemId)
    } yield x
    db.run(action.transactionally).map {
      case rowsAffected@0 => false
      case 1 => true
    }
  }

  def sell(lotId: LotId, seller: UserId, amount: Int): Future[(Int, Int)] = {
    val lot = lots.filter(x => x.id === lotId && x.userId =!= seller)
    val lotWithItem = for {
      (l, item) <- lot.join(stack).on(_.itemStackId === _.id)
    } yield (l, item)
    val action = lotWithItem.result.headOption.flatMap {
      case Some((_, item)) =>
        val amountToSell = if (item.stackValue < amount) item.stackValue else amount
        val remains = item.stackValue - amountToSell
        val update = stack.filter(_.id === item.id).update(item.copy(stackValue = remains))
        update.flatMap(_ => DBIO.successful((remains, amountToSell)))
      case _ => DBIO.failed(new IllegalStateException(s"Lot[$lot] is completed"))
    }

    db.run(action)
  }

  def revertSell(lotId: LotId, amountBack: Int): Future[Boolean] = {
    val lot = lots.filter(x => x.id === lotId)
    val lotWithItem = for {
      (l, item) <- lot.join(stack).on(_.itemStackId === _.id)
    } yield (l, item)

    val action = lotWithItem.result.headOption.flatMap {
      case Some((_, item)) =>
        val update = stack.filter(_.id === item.id).update(item.copy(stackValue = item.stackValue + amountBack))
        update.flatMap(_ => DBIO.successful(true))
      case _ => DBIO.failed(new IllegalStateException(s"Lot[$lot] is completed"))
    }
    db.run(action)
  }

  def delete(lot: LotId): Future[Boolean] = {
    val loaded = fullLoad(lots.filter(_.id === lot))
    val action = loaded.result.head.flatMap {
      case (_, buyout, minPrice, item, bidFlat, bidItem) =>
        val deletes = Seq(
          lots.filter(_.id === lot).delete,
          stack.filter(x => x.id === buyout.id || x.id === minPrice.id || x.id === item.id).delete
        )
        DBIO.sequence(deletes) >> DBIO.successful(true)
    }
    db.run(action.transactionally)
  }

  private def insertLotAction(lot: PlaceLot, withMinPrice: Option[DBIOAction[ItemId, NoStream, Effect.Write]] = None) = {
    val idReturn = stack returning stack.map(_.id)
    val lotIdReturn = lots returning lots.map(_.id)
    val (item, priceItem) = (lot.item, lot.buyout)
    for {
      itemId <- idReturn += ItemStackFlat(None, item.amount, item.id)
      priceId <- idReturn += ItemStackFlat(None, priceItem.amount, priceItem.id)
      minPriceId <- withMinPrice getOrElse DBIO.successful(priceId)
      x <- lotIdReturn += LotFlat(None, lot.user, itemId, priceId, minPriceId, lot.until, lot.tpe)
    } yield x
  }

  private def insertBidAction(lot: LotId, bid: Bid) = {
    val itemToAdd = bid.price
    val insertItemStack = stack.returning(stack.map(_.id))
      .+=(ItemStackFlat(None, itemToAdd.amount, itemToAdd.id))

    for {
      s <- insertItemStack
      b <- bids += BidFlat(Some(lot), bid.owner, s)
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
      (((((flat, buyouts), minPrice), items), bids), bidItems) <- query
    } yield (flat, buyouts, minPrice, items, bids, bidItems)

  }

  private def mapToLot(params: (LotFlat, ItemStackFlat, ItemStackFlat, ItemStackFlat, Option[BidFlat], Option[ItemStackFlat])): Lot = params match {
    case (lotFlat, buyout, minPrice, item, bidFlat, bidItem) =>
      lotFlat.tpe match {
        case dealer if dealer == Dealer.`type` => Dealer(lotFlat.id.get, lotFlat.user, item, buyout, lotFlat.until)
        case strict if strict == Strict.`type` => Strict(lotFlat.id.get, lotFlat.user, item, buyout, lotFlat.until)
        case nStrict if nStrict == NonStrict.`type` =>
          val mbBid = for {
            b <- bidFlat
            bItem <- bidItem
          } yield Bid(b.user, bItem)
          NonStrict(lotFlat.id.get, lotFlat.user, item, buyout, minPrice, lotFlat.until, mbBid)
      }
  }

}
