package ru.agny.xent.trade

import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.LazyLogging
import ru.agny.xent.core.Layer.LayerId
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.{PlainResponse, ResponseFailure, ResponseOk}
import ru.agny.xent.trade.Lot.LotId
import ru.agny.xent.trade.persistence.slick.LotRepository

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class Board(layer: LayerId, dbConfig: String) extends LazyLogging {

  import ru.agny.xent.trade.Board._

  private val lotRepository = LotRepository(dbConfig)

  def lots(start: Int = 0, end: Int = 50) = {
    val r = lotRepository.load(start, end)
    Source.fromPublisher(r)
  }

  def offer[T <: WSRequest : WSAdapter](msg: BoardMessage): Future[PlainResponse] = {
    logger.debug("got message {}", msg)
    msg match {
      case Add(lot) => addLot(lot)
      case Buy(lot, bid) => buy(lot, bid)
      case PlaceBid(lot, bid) => placeBid(lot, bid)
      case Sell(lot, amount, user) => sell(lot, amount, user)
    }
  }

  private def addLot[T <: WSRequest : WSAdapter](lot: PlaceLot): Future[PlainResponse] = {
    lotRepository.create(lot).flatMap(lotId =>
      verifyPlacement(lot.user, lot.item).map {
        case ResponseFailure =>
          lotRepository.delete(lotId).recover {
            case t: Throwable => logger.error("unverified record cleanup failed {}", t)
          }
          ResponseFailure
        case x => x
      }
    )
  }

  /**
    * Buy operation is considered complete if next conditions are met:
    * 1) lot to buy exists in database;
    * 2) buyer has sufficient funds to cover lot price;
    * 3) lot successfully deleted.
    *
    * 3-rd rule is significant due to transactional nature of operation, which I have to emulate because
    * I don't want to create long transaction locks (and they're will be long cause websocket connection
    * is created to ask another service about funds of buyer)
    *
    */
  private def buy[T <: WSRequest : WSAdapter](lotId: LotId, buyer: UserId): Future[PlainResponse] = {
    val wsAdapter = implicitly[WSAdapter[T]]
    val spend = (price: ItemHolder) => Spend(buyer, price)
    val prepareToSpend = for {
      lot <- lotRepository.buyPreparement(lotId, buyer)
      isAllowed <- wsAdapter.send("item_spend", spend(lot.item))
    } yield (lot, isAllowed)

    prepareToSpend.transformWith {
      case Success((lot, isAllowed)) => buyComplete(lot, buyer, isAllowed)
      case Failure(t) =>
        logger.error("lot buyout failed", t)
        Future.failed(t)
    }
  }

  private def placeBid[T <: WSRequest : WSAdapter](lot: LotId, bid: Bid): Future[PlainResponse] = {
    lotRepository.read(lot).flatMap {
      case Some(lotRead: NonStrict) if lotRead.tpe == NonStrict.`type` =>
        for {
          _ <- lotRepository.updateBid(lot, bid)
          verified <- verifyPlacement(bid.owner, bid.price)
          _ <- complete(verified,
            returnOldBidToOwner(lotRead.lastBid),
            lotRepository.revertBid(lot, bid, lotRead.lastBid))
        } yield verified
      case None =>
        logger.info("lot {} is not found", lot)
        Future.successful(ResponseFailure)
      case _ =>
        logger.warn("lot {} is not biddable", lot)
        Future.successful(ResponseFailure)
    }
  }

  private def sell[T <: WSRequest : WSAdapter](lotId: LotId, amount: Int, user: UserId): Future[PlainResponse] = {
    lotRepository.read(lotId).flatMap {
      case Some(lotRead: Dealer) if lotRead.tpe == Dealer.`type` =>
        val soldPrice = (sold: Int) => ItemHolder(lotRead.buyout.id, sold * lotRead.buyout.amount)
        val boughtItems = (sold: Int) => ItemHolder(lotRead.item.id, sold)
        for {
          sold <- lotRepository.sell(lotId, user, amount)
          verified <- verifyPlacement(user, soldPrice(sold))
          _ <- complete(verified,
            sendItems(lotRead, (lotRead.user, boughtItems(sold)), (user, soldPrice(sold))),
            lotRepository.revertSell(lotId, sold))
        } yield verified
      case None =>
        logger.info("lot {} is not found", lotId)
        Future.successful(ResponseFailure)
      case _ =>
        logger.warn("lot {} is not sellable", lotId)
        Future.successful(ResponseFailure)
    }
  }

  private def verifyPlacement[T <: WSRequest : WSAdapter](byUser: UserId, payment: ItemHolder): Future[PlainResponse] = {
    val wsAdapter = implicitly[WSAdapter[T]]
    wsAdapter.send("item_spend", Spend(byUser, payment)).flatMap {
      case ResponseOk => Future.successful(ResponseOk)
      case ResponseFailure =>
        logger.warn("item placement is denied for {}", (byUser, payment))
        Future.successful(ResponseFailure)
    }
  }

  private def complete(verification: PlainResponse,
                       success: => Future[Boolean],
                       cancellation: => Future[Boolean]) = {
    if (verification == ResponseFailure) cancellation else success
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
        logger.error("item is not delivered {}", x)
        lotRepository.reserveItem(msg.user, msg.items).recover {
          case t: Throwable => logger.error("reserve item {} failure {}", msg, t)
        }
        Future.successful(false)
      case _ => Future.successful(true)
    }
  }

  private def buyComplete[T <: WSRequest : WSAdapter](lot: Lot, buyer: UserId, verification: PlainResponse) = {
    verification match {
      case ResponseOk =>
        lotRepository.delete(lot.id).transform {
          case Success(true) =>
            sendItems(lot, (lot.user, lot.item), (buyer, lot.buyout))
            Success(ResponseOk)
          case x@(Failure(_) | Success(false)) =>
            logger.error("bought lot deletion failure {}", x)
            returnItemsToOwner(buyer, lot.buyout)
            Success(ResponseFailure)
        }
      case ResponseFailure =>
        logger.warn("buy operation is denied for buyer {} and lot {}", buyer, lot)
        Future.successful(ResponseFailure)
    }
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
