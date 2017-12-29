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

  def offer(msg: FlowMessage) = {
    logger.debug("got message {}", msg)
    msg match {
      case Add(lot) => Source.fromFuture(addLot(lot))
      case Buy(lot, bid) => Source.fromFuture(buy(lot, bid))
      case PlaceBid(lot, bid) => Source.fromFuture(placeBid(lot, bid))
    }
  }

  private def addLot(lot: PlaceLot): Future[PlainResponse] = {
    lotRepository.create(lot).flatMap(lotId =>
      verifyPlacement(lot.user, lot.item).flatMap {
        case ResponseFailure =>
          lotRepository.delete(lotId).recover {
            case t: Throwable => logger.error("cleanup unverified record failed {}", t)
          }
          Future.successful(ResponseFailure)
        case ResponseOk => Future.successful(ResponseOk)
      }
    )
  }

  private def buy(lot: LotId, bid: Bid): Future[PlainResponse] = {
    val spend = Spend(bid.owner, bid.price.amount)
    val prepareToSpend = for {
      (seller, item) <- lotRepository.buy(lot, bid)
      isAllowed <- WSClient.send("item_spend", spend)
    } yield (seller, item, isAllowed)

    prepareToSpend.flatMap { case (seller, item, isAllowed) =>
      isAllowed match {
        case ResponseOk =>
          lotRepository.delete(lot).recover {
            case t: Throwable => logger.error("bought lot deletion failure {}", t); false
          }
          sendItem(Receive(bid.owner, item))
          sendItem(Receive(seller, bid.price.amount))

          Future.successful(ResponseOk)
        case ResponseFailure =>
          logger.info("buy operation is denied for {}", spend)
          Future.successful(ResponseFailure)
      }
    }
  }

  private def placeBid(lot: LotId, bid: Bid): Future[PlainResponse] = {
    lotRepository.read(lot).flatMap {
      case Some(lotRead: NonStrict) if lotRead.tpe == NonStrict.`type` =>
        for {
          _ <- lotRepository.updateBid(lot, bid)
          verified <- verifyPlacement(bid.owner, bid.price.amount)
          _ <- if (verified == ResponseFailure) lotRepository.revertBid(lot, bid, lotRead.lastBid) else Future.successful(false)
        } yield verified
      case None => Future.successful(ResponseFailure)
      case _ =>
        logger.error("lot {} is not biddable", lot)
        Future.successful(ResponseFailure)
    }
  }

  private def verifyPlacement(byUser: UserId, payment: ItemStack): Future[PlainResponse] = {
    WSClient.send("item_spend", Spend(byUser, payment)).flatMap {
      case ResponseOk => Future.successful(ResponseOk)
      case ResponseFailure =>
        logger.error("item placement is denied for {}", (byUser, payment))
        Future.successful(ResponseFailure)
    }
  }

  private def sendItem(msg: Receive): Future[Boolean] = {
    val sendResult = WSClient.send("item_receive", msg)
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
  sealed trait FlowMessage
  final case class Add(lot: PlaceLot) extends FlowMessage
  final case class Buy(lot: LotId, bid: Bid) extends FlowMessage
  final case class PlaceBid(lot: LotId, bid: Bid) extends FlowMessage

  sealed trait ItemCommand
  final case class Spend(user: UserId, items: ItemStack) extends ItemCommand
  final case class Receive(user: UserId, items: ItemStack) extends ItemCommand
}
