package ru.agny.xent.trade

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
import ru.agny.xent.core.Layer.LayerId
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.trade.persistence.slick.LotRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class Board(layer: LayerId) {

  import ru.agny.xent.trade.Board._

  implicit val system = ActorSystem("boards")
  implicit val materializer = ActorMaterializer()

  private val source: Source[Message, SourceQueueWithComplete[Message]] = Source.queue(100, OverflowStrategy.backpressure)
  private val sink: Sink[Message, Future[Done]] = Sink.foreach[Message] {
    case Buy(lot, user) => Future {
      LotRepository.delete(lot)
    }
    case PlaceBid(lot, bid) =>
      LotRepository.read(lot).flatMap {
        case Some(v) if v.tpe == NonStrict.`type` =>
          val nonStrictLot = v.asInstanceOf[NonStrict]
          LotRepository.updateBid(nonStrictLot.copy(lastBid = Some(bid)))
        case _ => Future.failed(new IllegalArgumentException(s"Input lot[id:$lot] is not biddable")) // TODO stream passing error messages to client?
      }
    case Add(lot) => LotRepository.create(lot)
  }
  private val queue: SourceQueueWithComplete[Message] = source.to(sink).run

  def lots(start: Int = 0, end: Int = 50) = Source.fromPublisher(LotRepository.load(start, end))

  def offer(msg: Message) = queue.offer(msg)

}

object Board {
  sealed trait Message
  final case class Add(lot: Lot) extends Message
  final case class Buy(lot: ItemId, by: UserId) extends Message
  final case class PlaceBid(lot: ItemId, bid: Bid) extends Message
}
