package ru.agny.xent.trade

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
import ru.agny.xent.core.Layer.LayerId
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.utils.UserType.UserId

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class Board(layer: LayerId) {

  import ru.agny.xent.trade.Board._

  implicit val system = ActorSystem("boards")
  implicit val materializer = ActorMaterializer()

  private val source: Source[Message, SourceQueueWithComplete[Message]] = Source.queue(100, OverflowStrategy.backpressure)
  private val sink: Sink[Message, Future[Done]] = Sink.foreach[Message] {
    case Buy(lot, user) => Future {
      Persistence.delete(lot)
    }
    case PlaceBid(lot, bid) => Future {
      Persistence.read(lot) match {
        case v: NonStrict => Persistence.update(lot, v.copy(lastBid = Some(bid)))
        case x => x
      }
    }
    case Add(lot) => Future {
      Persistence.create(lot)
    }
  }
  private val queue: SourceQueueWithComplete[Message] = source.to(sink).run

  def lots(start: Int = 0, end: Int = 50) = Persistence.source()

  def offer(msg: Message) = queue.offer(msg)

}

object Board {
  sealed trait Message
  final case class Add(lot: Lot) extends Message
  final case class Buy(lot: ItemId, by: UserId) extends Message
  final case class PlaceBid(lot: ItemId, bid: Bid) extends Message
}
