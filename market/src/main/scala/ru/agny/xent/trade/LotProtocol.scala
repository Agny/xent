package ru.agny.xent.trade

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import io.circe.{Decoder, Encoder}
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

import scala.concurrent.Future

object LotProtocol {

  val `xent.api.v1+json` = MediaType.applicationWithFixedCharset("xent.api.v1+json", HttpCharsets.`UTF-8`)
  val ct = ContentType.apply(`xent.api.v1+json`)

  implicit val encodeLot: Encoder[Lot] = Encoder.forProduct7("id", "user", "item", "buyout", "until", "lastBid", "type") { lot: Lot =>
    lot match {
      case v@(_: Strict | _: Dealer) => (v.id, v.user, v.item, v.buyout, v.until, None, v.getClass.getSimpleName)
      case n: NonStrict => (n.id, n.user, n.item, n.buyout, n.until, n.lastBid, n.getClass.getSimpleName)
    }
  }

  implicit val decodeLot: Decoder[Lot] = {
    def collectLot(id: ItemId,
                   user: UserId,
                   item: ItemStack,
                   buyout: Price,
                   until: TimeStamp,
                   lastBid: Option[Bid],
                   `type`: String): Lot = {
      `type` match {
        case v if v == Dealer.toString() => Dealer(id, user, item, buyout, until)
        case v if v == Strict.toString() => Strict(id, user, item, buyout, until)
        case v if v == NonStrict.toString() => NonStrict(id, user, item, buyout, until, lastBid)
      }
    }

    Decoder.forProduct7("id", "user", "item", "buyout", "until", "lastBid", "type")(collectLot)
  }


  implicit def lotMarshaller: ToEntityMarshaller[Lot] = Marshaller.oneOf(
    Marshaller.withFixedContentType(ct) { lot ⇒
      HttpEntity(ct, lot.asJson.noSpaces)
    }
  )

  implicit def lotUnmarshaller: FromEntityUnmarshaller[Lot] = Unmarshaller.stringUnmarshaller.flatMap(ctx => mat => json =>
    decode[Lot](json).fold(Future.failed, Future.successful)
  )

}
