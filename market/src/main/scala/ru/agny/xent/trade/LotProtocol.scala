package ru.agny.xent.trade

import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentType, HttpCharsets, HttpEntity, MediaType}
import io.circe.Encoder

object LotProtocol {

  val `vnd.example.api.v1+json` = MediaType.applicationWithFixedCharset("vnd.example.api.v1+json", HttpCharsets.`UTF-8`)
  val ct = ContentType.apply(`vnd.example.api.v1+json`)

  implicit val encodeUser: Encoder[Lot] =
    Encoder.forProduct6("id", "user", "item", "buyout", "until", "lastBid") { lot: Lot =>
      lot match {
        case s@(_: Strict | _: Dealer) => (s.id, s.user, s.item, s.buyout, s.until, None)
        case n: NonStrict => (n.id, n.user, n.item, n.buyout, n.until, n.lastBid)
      }
    }

  implicit def lotMarshaller: ToEntityMarshaller[Lot] = Marshaller.oneOf(
    Marshaller.withFixedContentType(`vnd.example.api.v1+json`) { lot ⇒
      HttpEntity(`vnd.example.api.v1+json`, lot.asJson.noSpaces)
    })

}
