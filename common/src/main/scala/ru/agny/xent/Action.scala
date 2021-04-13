package ru.agny.xent

import io.circe.{Codec, Decoder, Encoder, HCursor, Json}
import io.circe.syntax._
import io.circe.parser._

enum Action:
  case Noop
  case Op(v: String)
end Action

object Action {

  given Encoder[Action] = {
    case Noop => Json.obj(
      ("type", Json.fromString("Noop"))
    )
    case v@Op(s) => Json.obj(
      ("type", Json.fromString(v.getClass.getSimpleName))
    )
  }

  given Decoder[Op] = { (c: HCursor) =>
    for {
      v <- c.downField("v").as[String]
    } yield Op(v).asInstanceOf[Op]
  }

  given Decoder[Action] = { (c: HCursor) =>
    for {
      discriminator <- c.downField("type").as[String]
      event <- discriminator match {
        case "Noop" => Decoder.const(Noop)(c)
        case "Op" => c.value.as[Op]
      }
    } yield event
  }
}
