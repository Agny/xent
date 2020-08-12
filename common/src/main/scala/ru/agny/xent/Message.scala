package ru.agny.xent

import io.circe.{Codec, Decoder, Encoder, HCursor, Json}
import io.circe.syntax._
import io.circe.parser._

enum Message {
  case Event(actor: UserId, timestamp: Long, action: Action)
  case Notification(actor: UserId, target: UserId, timestamp: Long, action: Action)
}
object Message {

  given Encoder[Event] = { (a: Event) =>
    Json.obj(
      ("type", Json.fromString(classOf[Event].getSimpleName)),
      ("actor", Json.fromLong(a.actor)),
      ("timestamp", Json.fromLong(a.timestamp)),
      ("action", a.action.asJson)
    )
  }

  given Encoder[Notification] = { (a: Notification) =>
    Json.obj(
      ("type", Json.fromString(classOf[Notification].getSimpleName)),
      ("actor", Json.fromLong(a.actor)),
      ("target", Json.fromLong(a.target)),
      ("timestamp", Json.fromLong(a.timestamp)),
      ("action", a.action.asJson)
    )
  }

  given Encoder[Message] = { (a: Message) =>
    a match {
      case v@Event(actor, timestamp, action) => v.asJson
      case v@Notification(actor, timestamp, target, action) => v.asJson
    }
  }

  given Decoder[Event] = { (c: HCursor) =>
    for {
      actor <- c.downField("actor").as[UserId]
      timestamp <- c.downField("timestamp").as[Long]
      action <- c.downField("action").as[Action]
    } yield Event(actor, timestamp, action).asInstanceOf[Event]
  }

  given Decoder[Notification] = { (c: HCursor) =>
    for {
      actor <- c.downField("actor").as[UserId]
      target <- c.downField("target").as[UserId]
      timestamp <- c.downField("timestamp").as[Long]
      action <- c.downField("action").as[Action]
    } yield Notification(actor, target, timestamp, action).asInstanceOf[Notification]
  }  

  given Decoder[Message] = { (c: HCursor) =>
    for {
      discriminator <- c.downField("type").as[String]
      event <- discriminator match {
        case "Event" => c.value.as[Event]
        case "Notification" => c.value.as[Notification]
      }
    } yield event
  }
}
