package ru.agny.xent.trade

import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.LazyLogging
import ru.agny.xent.core.Layer.LayerId
import ru.agny.xent.core.inventory.ItemStack
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

  private def buy[T <: WSRequest : WSAdapter](lotId: LotId, bid: Bid): Future[PlainResponse] = {
    val wsAdapter = implicitly[WSAdapter[T]]
    val spend = Spend(bid.owner, bid.price.amount)
    val prepareToSpend = for {
      lot <- lotRepository.buy(lotId, bid)
      isAllowed <- wsAdapter.send("item_spend", spend)
    } yield (lot, isAllowed)

    prepareToSpend.transformWith {
      case Success((lot, isAllowed)) =>
        isAllowed match {
          case ResponseOk =>
            lotRepository.delete(lotId).recover {
              case t: Throwable => logger.error("bought lot deletion failure {}", t)
            }
            sendItems(lot, bid)
            Future.successful(ResponseOk)
          case ResponseFailure =>
            logger.warn("buy operation is denied for {}", spend)
            Future.successful(ResponseFailure)
        }
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
          verified <- verifyPlacement(bid.owner, bid.price.amount)
          _ <- revertBidIfNeeded(verified, lotRead, bid)
        } yield verified
      case None =>
        logger.info("lot {} is not found", lot)
        Future.successful(ResponseFailure)
      case _ =>
        logger.warn("lot {} is not biddable", lot)
        Future.successful(ResponseFailure)
    }
  }

  private def verifyPlacement[T <: WSRequest : WSAdapter](byUser: UserId, payment: ItemStack): Future[PlainResponse] = {
    val wsAdapter = implicitly[WSAdapter[T]]
    wsAdapter.send("item_spend", Spend(byUser, payment)).flatMap {
      case ResponseOk => Future.successful(ResponseOk)
      case ResponseFailure =>
        logger.warn("item placement is denied for {}", (byUser, payment))
        Future.successful(ResponseFailure)
    }
  }

  private def revertBidIfNeeded[T <: WSRequest : WSAdapter](verification: PlainResponse, lot: NonStrict, toRevert: Bid) = {
    if (verification == ResponseFailure) lotRepository.revertBid(lot.id, toRevert, lot.lastBid)
    else returnBidToOwner(lot.lastBid)
  }

  private def sendItems[T <: WSRequest : WSAdapter](lot: Lot, bid: Bid): Future[Boolean] = {
    lot match {
      case x: NonStrict =>
        sendReceive(Receive(bid.owner, lot.item))
        sendReceive(Receive(lot.user, bid.price.amount))
        returnBidToOwner(x.lastBid)
      case _ =>
        sendReceive(Receive(bid.owner, lot.item))
        sendReceive(Receive(lot.user, bid.price.amount))
    }
  }

  private def returnBidToOwner[T <: WSRequest : WSAdapter](mbBid: Option[Bid]): Future[Boolean] = mbBid match {
    case Some(v) => sendReceive(Receive(v.owner, v.price.amount))
    case None => Future.successful(true)
  }

  private def sendReceive[T <: WSRequest : WSAdapter](msg: Receive): Future[Boolean] = {
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
}

object Board {
  sealed trait BoardMessage
  final case class Add(lot: PlaceLot) extends BoardMessage
  final case class Buy(lot: LotId, bid: Bid) extends BoardMessage
  final case class PlaceBid(lot: LotId, bid: Bid) extends BoardMessage

  sealed trait ItemCommand
  final case class Spend(user: UserId, items: ItemStack) extends ItemCommand
  final case class Receive(user: UserId, items: ItemStack) extends ItemCommand
}
