package ru.agny.xent.trade

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.circe.generic.auto._
import io.circe.syntax._
import ru.agny.xent.messages.PlainResponse
import ru.agny.xent.trade.Board.ItemCommand
import ru.agny.xent.web.IncomeMessage

import scala.concurrent.Future

trait WSAdapter[Request <: WSRequest] {

  def build(msg: IncomeMessage): Request

  def send(tpe: String, msg: ItemCommand): Future[PlainResponse]
}

object WSAdapter {
  implicit object AkkaRequestBuilder extends WSAdapter[AkkaWSRequest] {
    implicit val system = ActorSystem("api")
    val materializer = ActorMaterializer()

    override def build(msg: IncomeMessage) = AkkaWSRequest(msg)

    override def send(tpe: String, msg: ItemCommand) = {
      val toSend = IncomeMessage(tpe, msg.asJson.noSpaces)
      build(toSend).out
    }
  }
}
