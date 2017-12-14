package ru.agny.xent.trade

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
import ru.agny.xent.core.Layer.LayerId
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.trade.persistence.slick.LotRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class Board(layer: LayerId, dbConfig: String) {

  import ru.agny.xent.trade.Board._

  implicit val system = ActorSystem("boards")
  implicit val materializer = ActorMaterializer()

  private val lotRepository = LotRepository(dbConfig)

  private val source: Source[Message, SourceQueueWithComplete[Message]] = Source.queue(100, OverflowStrategy.backpressure)
  private val sink: Sink[Message, Future[Done]] = Sink.foreach[Message] {
    case Buy(lot, bid) => lotRepository.buy(lot, bid)
    case PlaceBid(lot, bid) =>
      lotRepository.read(lot).flatMap {
        case Some(v) if v.tpe == NonStrict.`type` => lotRepository.updateBid(v.id, bid)
        case _ => Future.failed(new IllegalArgumentException(s"Input lot[id:$lot] is not biddable")) // TODO stream passing error messages to client?
      }
    case Add(lot) => lotRepository.create(lot)
  }
  private val queue: SourceQueueWithComplete[Message] = source.to(sink).run

  def lots(start: Int = 0, end: Int = 50) = Source.fromPublisher(lotRepository.load(start, end))

  def offer(msg: Message) = queue.offer(msg)

}

object Board {
  sealed trait Message
  final case class Add(lot: PlaceLot) extends Message
  final case class Buy(lot: ItemId, bid: Bid) extends Message
  final case class PlaceBid(lot: ItemId, bid: Bid) extends Message
}
