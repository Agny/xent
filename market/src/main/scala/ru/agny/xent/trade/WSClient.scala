package ru.agny.xent.trade

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import ru.agny.xent.messages.PlainResponse
import ru.agny.xent.trade.Board.ItemCommand
import ru.agny.xent.web.IncomeMessage

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class WSClient(msg: IncomeMessage) {

  private val outgoing = Source.single(TextMessage(msg.asJson.noSpaces))
  private val incoming = Sink.head[Message]

  private val webSocketFlow = Http()(WSClient.system).webSocketClientFlow(WebSocketRequest("ws://localhost:8888"))

  private val (upgrade, response) = outgoing
    .viaMat(webSocketFlow)(Keep.right)
    .toMat(incoming)(Keep.both)
    .run()(WSClient.materializer)

  private val connected = upgrade.flatMap { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      Future.successful(Done)
    } else {
      Future.failed(new RuntimeException(s"Connection failed: ${upgrade.response.status}"))
    }
  }

  private val decoded = response.flatMap {
    case TextMessage.Strict(body) =>
      decode[PlainResponse](body) match {
        case Left(v) => Future.failed(v)
        case Right(v) => Future.successful(v)
      }
  }
  val result = for {
    _ <- connected
    x <- decoded
  } yield x

}

object WSClient {
  implicit val system = ActorSystem("api")
  val materializer = ActorMaterializer()

  def send(tpe: String, msg: ItemCommand): Future[PlainResponse] = {
    val toSend = IncomeMessage(tpe, msg.asJson.noSpaces)
    WSClient(toSend).result
  }
}
