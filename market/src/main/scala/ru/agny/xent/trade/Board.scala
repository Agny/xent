package ru.agny.xent.trade

import com.typesafe.scalalogging.LazyLogging
import ru.agny.xent.core.Layer.LayerId
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.{PlainResponse, ResponseFailure, ResponseOk}
import ru.agny.xent.trade.Lot.LotId
import ru.agny.xent.trade.persistence.slick.LotRepository
import ru.agny.xent.trade.utils.{SynchronizableTask, SynchronizedPool}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

case class Board(layer: LayerId, dbConfig: String) extends LazyLogging {

  import ru.agny.xent.trade.Board._
  import scala.concurrent.duration._

  private val lotRepository = LotRepository(dbConfig)

  def lots(start: Int = 0, end: Int = 50) = lotRepository.load(start, end)

  def offer[T <: WSRequest : WSAdapter](msg: BoardMessage)(implicit pool: SynchronizedPool): Future[PlainResponse] = {
    logger.debug("got message {}", msg)
    msg match {
      case Add(lot) => addLot(lot)
      case Buy(lot, bid) => pool.submit(buy(lot, bid))
      case PlaceBid(lot, bid) => pool.submit(placeBid(lot, bid))
      case Sell(lot, amount, user) => pool.submit(sell(lot, amount, user))
    }
  }

  private def addLot[T <: WSRequest : WSAdapter](lot: PlaceLot): Future[PlainResponse] = {
    lotRepository.create(lot).flatMap(lotId =>
      verifyPlacement(lot.user, lot.item).flatMap {
        case ResponseFailure =>
          lotRepository.delete(lotId).map(_ => ResponseFailure).recover {
            case t: Throwable =>
              logger.error("unverified record cleanup failed {}", t)
              ResponseFailure
          }
        case x => Future.successful(x)
      }
    )
  }

  private def buy[T <: WSRequest : WSAdapter](lotId: LotId, buyer: UserId) = {
    val result = lotRepository.read(lotId).flatMap {
      case None =>
        logger.info("lot {} is not found", lotId)
        Future.successful(ResponseFailure)
      case Some(lot) if lot.tpe == Dealer.`type` =>
        logger.warn("incorrect lot type {} to buy", lot)
        Future.successful(ResponseFailure)
      case Some(lot) if lot.user == buyer =>
        logger.warn("user can't buy {} from himself", lot)
        Future.successful(ResponseFailure)
      case Some(lot) =>
        verifyPlacement(buyer, lot.item).transformWith {
          case Success(verified) => buyComplete(lot, buyer, verified)
          case Failure(t) =>
            logger.error("buyout failed", t.getMessage)
            Future.failed(t)
        }
    }
    new SynchronizableTask(lotId, Await.result(result, 4 seconds))
  }

  private def placeBid[T <: WSRequest : WSAdapter](lotId: LotId, bid: Bid) = {
    val result = lotRepository.read(lotId).flatMap {
      case None =>
        logger.info("lot {} is not found", lotId)
        Future.successful(ResponseFailure)
      case Some(lot) if lot.tpe != NonStrict.`type` =>
        logger.warn("{} is not biddable", lot)
        Future.successful(ResponseFailure)
      case Some(lot) if lot.user == bid.owner =>
        logger.warn("user can't buy {} from himself", lot)
        Future.successful(ResponseFailure)
      case Some(lot: NonStrict) =>
        lot match {
          case NonStrict(_, _, _, _, minPrice, _, _) if minPrice.amount > bid.price.amount =>
            logger.warn("{} is less than minimal lot price {}", bid, lot)
            Future.successful(ResponseFailure)
          case NonStrict(_, _, _, _, _, _, Some(last)) if last.price.amount >= bid.price.amount =>
            logger.warn("{} is less than last bid price {}", bid, lot.lastBid)
            Future.successful(ResponseFailure)
          case ok =>
            for {
              verified <- verifyPlacement(bid.owner, bid.price)
              _ <- bidPlacingComplete(lot, bid, verified)
            } yield verified
        }
    }
    new SynchronizableTask(lotId, Await.result(result, 4 seconds))
  }

  private def sell[T <: WSRequest : WSAdapter](lotId: LotId, amount: Int, user: UserId) = {
    val result = lotRepository.read(lotId).flatMap {
      case None =>
        logger.info("lot {} is not found", lotId)
        Future.successful(ResponseFailure)
      case Some(lot) if lot.tpe != Dealer.`type` =>
        logger.warn("lot {} is not sellable", lot)
        Future.successful(ResponseFailure)
      case Some(lot) if lot.item.amount <= 0 =>
        logger.warn("lot {} is complete", lot)
        Future.successful(ResponseFailure)
      case Some(lot) if lot.user == user =>
        logger.warn("user can't sell {} to himself", lot)
        Future.successful(ResponseFailure)
      case Some(lot: Dealer) =>
        val soldPrice = (sold: Int) => ItemHolder(lot.buyout.id, sold * lot.buyout.amount)
        val boughtItems = (sold: Int) => ItemHolder(lot.item.id, sold)
        for {
          (remains, sold) <- lotRepository.sell(lotId, user, amount)
          verified <- verifyPlacement(user, soldPrice(sold))
          _ <- complete(verified == ResponseOk,
            sellComplete(lot, remains, boughtItems(sold), user, soldPrice(sold)),
            lotRepository.revertSell(lotId, sold))
        } yield verified
    }
    new SynchronizableTask(lotId, Await.result(result, 4 seconds))
  }

  private def verifyPlacement[T <: WSRequest : WSAdapter](byUser: UserId, payment: ItemHolder): Future[PlainResponse] = {
    val wsAdapter = implicitly[WSAdapter[T]]
    val msg = Spend(byUser, payment)
    logger.debug("Got message to send {}", msg)
    wsAdapter.send("item_spend", msg).flatMap {
      case ResponseOk => Future.successful(ResponseOk)
      case ResponseFailure =>
        logger.warn("item placement is denied for {}", (byUser, payment))
        Future.successful(ResponseFailure)
    }
  }

  private def complete(p: => Boolean,
                       success: => Future[Boolean],
                       cancellation: => Future[Boolean]) = {
    if (p) success else cancellation
  }

  private def sendItems[T <: WSRequest : WSAdapter](lot: Lot, itemOut: (UserId, ItemHolder), itemIn: (UserId, ItemHolder)): Future[Boolean] = {
    sendReceive(Receive(itemIn._1, itemOut._2))
    sendReceive(Receive(itemOut._1, itemIn._2))
    lot match {
      case x: NonStrict => returnOldBidToOwner(x.lastBid)
      case _ => Future.successful(true)
    }
  }

  private def returnOldBidToOwner[T <: WSRequest : WSAdapter](mbBid: Option[Bid]): Future[Boolean] = mbBid match {
    case Some(v) => returnItemsToOwner(v.owner, v.price)
    case None => Future.successful(true)
  }

  private def returnItemsToOwner[T <: WSRequest : WSAdapter](owner: UserId, items: ItemHolder): Future[Boolean] = {
    sendReceive(Receive(owner, items))
  }

  private def sendReceive[T <: WSRequest : WSAdapter](msg: Receive): Future[Boolean] = {
    logger.debug("Got message to send {}", msg)
    val wsAdapter = implicitly[WSAdapter[T]]
    val sendResult = wsAdapter.send("item_receive", msg)
    sendResult.transformWith {
      case x@(Success(ResponseFailure) | Failure(_)) =>
        logger.error("{} is not delivered {}", msg, x)
        lotRepository.reserveItem(msg.user, msg.items).recover {
          case t: Throwable => logger.error("reserve item {} failure {}", msg, t.getMessage)
        }
        Future.successful(false)
      case _ => Future.successful(true)
    }
  }

  private def buyComplete[T <: WSRequest : WSAdapter](lot: Lot, buyer: UserId, verification: PlainResponse) = {
    verification match {
      case ResponseOk =>
        lotRepository.delete(lot.id).transformWith {
          case Success(true) =>
            sendItems(lot, (lot.user, lot.item), (buyer, lot.buyout)).map(_ => ResponseOk)
          case x@(Failure(_) | Success(false)) =>
            logger.error("bought lot deletion failure {}", x)
            returnItemsToOwner(buyer, lot.buyout).map(_ => ResponseFailure)
        }
      case ResponseFailure =>
        logger.warn("buy operation is denied for buyer {} and lot {}", buyer, lot)
        Future.successful(ResponseFailure)
    }
  }

  private def bidPlacingComplete[T <: WSRequest : WSAdapter](lot: NonStrict,
                                                             bid: Bid,
                                                             verification: PlainResponse): Future[PlainResponse] = {
    verification match {
      case ResponseOk =>
        val updateResult = for {
          res <- lotRepository.updateBid(lot.id, bid)
          _ <- returnOldBidToOwner(lot.lastBid)
        } yield res

        updateResult.transformWith {
          case Success(true) => Future.successful(ResponseOk)
          case x@(Failure(_) | Success(false)) =>
            logger.error("{} update by bid {} failure {}", lot, bid, x)
            returnItemsToOwner(bid.owner, bid.price).map(_ => ResponseFailure)
        }
      case x =>
        logger.warn("{} update is denied for bid {}", lot, bid)
        Future.successful(x)
    }
  }

  private def sellComplete[T <: WSRequest : WSAdapter](lot: Lot, remains: Int, bought: ItemHolder, seller: UserId, sold: ItemHolder) = {
    for {
      _ <- complete(remains == 0, lotRepository.delete(lot.id), Future.successful(true))
      x <- sendItems(lot, (lot.user, bought), (seller, sold))
    } yield x
  }
}

object Board {
  sealed trait BoardMessage
  final case class Add(lot: PlaceLot) extends BoardMessage
  final case class Buy(lot: LotId, buyer: UserId) extends BoardMessage
  final case class PlaceBid(lot: LotId, bid: Bid) extends BoardMessage
  final case class Sell(toLot: LotId, amount: Int, user: UserId) extends BoardMessage

  sealed trait ItemCommand
  final case class Spend(user: UserId, items: ItemHolder) extends ItemCommand
  final case class Receive(user: UserId, items: ItemHolder) extends ItemCommand
}
