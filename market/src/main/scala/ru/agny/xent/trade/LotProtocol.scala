package ru.agny.xent.trade

import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentType, HttpCharsets, HttpEntity, MediaType}
import io.circe.{Decoder, Encoder}
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

object LotProtocol {

  val `vnd.example.api.v1+json` = MediaType.applicationWithFixedCharset("vnd.xent.api.v1+json", HttpCharsets.`UTF-8`)
  val ct = ContentType.apply(`vnd.example.api.v1+json`)

  implicit val encodeLot: Encoder[Lot] = Encoder.forProduct7("id", "user", "item", "buyout", "until", "lastBid", "type") { lot: Lot =>
    lot match {
      case v@(_: Strict | _: Dealer) => (v.id, v.user, v.item, v.buyout, v.until, None, v.getClass.toString)
      case n: NonStrict => (n.id, n.user, n.item, n.buyout, n.until, n.lastBid, n.getClass.toString)
    }
  }

  implicit val decodeLot: Decoder[Lot] =
    Decoder[(ItemId, UserId, ItemStack, Price, TimeStamp, Option[Bid], String)].map {
      case (id, user, item, buyout, until, _, tpe) if tpe == Dealer.getClass.getName => Dealer(id, user, item, buyout, until)
      case (id, user, item, buyout, until, _, tpe) if tpe == Strict.getClass.getName => Strict(id, user, item, buyout, until)
      case (id, user, item, buyout, until, lastBid, tpe) if tpe == NonStrict.getClass.getName => NonStrict(id, user, item, buyout, until, lastBid)
    }


  implicit def lotMarshaller: ToEntityMarshaller[Lot] = Marshaller.oneOf(
    Marshaller.withFixedContentType(ct) { lot ⇒
      HttpEntity(ct, lot.asJson.noSpaces)
    }
  )
}
