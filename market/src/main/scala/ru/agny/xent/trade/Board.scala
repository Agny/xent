package ru.agny.xent.trade

import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.LazyLogging
import ru.agny.xent.core.Layer.LayerId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.trade.Lot.LotId
import ru.agny.xent.trade.persistence.slick.LotRepository

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

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
      case Add(lot) =>
        val isSuccess = for {
          _ <- lotRepository.create(lot)
          wsReply <- WSClient.send("item_spend", Spend(lot.user, lot.item))
        } yield wsReply
        Source.fromFuture(isSuccess)
      case Buy(lot, bid) =>
        val responses = lotRepository.buy(lot, bid).flatMap { case (seller, item) =>
          val wsBuyerSpend = WSClient.send("item_spend", Spend(bid.owner, bid.price.amount))
          val wsBuyerReceive = WSClient.send("item_receive", Receive(bid.owner, item))
          val wsSellerReceive = WSClient.send("item_receive", Receive(seller, bid.price.amount))
          for {
            a <- wsBuyerSpend
            b <- wsBuyerReceive
            c <- wsSellerReceive
          } yield (a, b, c)
        }
        //TODO handle failed responses
        Source.fromFuture(responses.map(x => x._1))
      case PlaceBid(lot, bid) =>
        val isSuccess = for {
          _ <- lotRepository.read(lot).flatMap {
            case Some(v) if v.tpe == NonStrict.`type` => lotRepository.updateBid(v.id, bid)
            case _ => Future.failed(new IllegalArgumentException(s"Input lot[id:$lot] is not biddable"))
          }
          wsReply <- WSClient.send(???, ???)
        } yield wsReply
        Source.fromFuture(isSuccess)
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
