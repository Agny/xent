package ru.agny.xent.trade

import io.circe.generic.auto._
import io.circe.parser._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}

import scala.concurrent.Future

object BidProtocol {

  implicit def bidUnmarshaller: FromEntityUnmarshaller[Bid] = Unmarshaller.stringUnmarshaller.flatMap(ctx => mat => json =>
    decode[Bid](json).fold(Future.failed, Future.successful)
  )

}
