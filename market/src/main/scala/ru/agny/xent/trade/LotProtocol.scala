package ru.agny.xent.trade

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import io.circe.Encoder
import ru.agny.xent.messages.PlainResponse

import scala.concurrent.Future

object LotProtocol {

  val `xent.api.v1+json` = MediaType.applicationWithFixedCharset("xent.api.v1+json", HttpCharsets.`UTF-8`)
  val ct = ContentType.apply(`xent.api.v1+json`)

  implicit val encodeLot: Encoder[Lot] = Encoder.forProduct7("id", "user", "item", "buyout", "until", "lastBid", "type") { lot: Lot =>
    lot match {
      case v@(_: Strict | _: Dealer) => (v.id, v.user, v.item, v.buyout, v.until, None, v.tpe.v)
      case n: NonStrict => (n.id, n.user, n.item, n.buyout, n.until, n.lastBid, n.tpe.v)
    }
  }

  implicit def lotMarshaller: ToEntityMarshaller[Lot] = Marshaller.oneOf(
    Marshaller.withFixedContentType(ct) { lot ⇒
      HttpEntity(ct, lot.asJson.noSpaces)
    }
  )

  implicit def responseMarshaller: ToEntityMarshaller[PlainResponse] = Marshaller.oneOf(
    Marshaller.withFixedContentType(ct) { s ⇒
      HttpEntity(ct, ByteString(s.value))
    }
  )

  implicit def placelotUnmarshaller: FromEntityUnmarshaller[PlaceLot] = Unmarshaller.stringUnmarshaller.flatMap(ctx => mat => json =>
    decode[PlaceLot](json).fold(Future.failed, Future.successful)
  )

}
