package ru.agny.xent.trade

import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.LazyLogging
import ru.agny.xent.core.Layer.LayerId
import ru.agny.xent.core.inventory.Item.ItemId
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

  def offer(msg: FlowMessage) = msg match {
    case Add(lot) =>
      val isSuccess = for {
        _ <- lotRepository.create(lot)
        wsReply <- WSClient.send("Add")
      } yield wsReply
      Source.fromFuture(isSuccess)
    case Buy(lot, bid) =>
      val isSuccess = for {
        _ <- lotRepository.buy(lot, bid)
        wsReply <- WSClient.send("Buy")
      } yield wsReply
      Source.fromFuture(isSuccess)
    case PlaceBid(lot, bid) =>
      val isSuccess = for {
        _ <- lotRepository.read(lot).flatMap {
          case Some(v) if v.tpe == NonStrict.`type` => lotRepository.updateBid(v.id, bid)
          case _ => Future.failed(new IllegalArgumentException(s"Input lot[id:$lot] is not biddable"))
        }
        wsReply <- WSClient.send("Bid")
      } yield wsReply
      Source.fromFuture(isSuccess)
  }

}

object Board {
  sealed trait FlowMessage
  final case class Add(lot: PlaceLot) extends FlowMessage
  final case class Buy(lot: ItemId, bid: Bid) extends FlowMessage
  final case class PlaceBid(lot: ItemId, bid: Bid) extends FlowMessage
}
