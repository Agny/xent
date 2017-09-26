package ru.agny.xent.web.utils

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import ru.agny.xent.core.utils.UserIdGenerator
import ru.agny.xent.messages._
import ru.agny.xent.messages.production._
import ru.agny.xent.messages.unit.{CreateSoulMessage, CreateTroopMessage}
import ru.agny.xent.web.IncomeMessage

object JsonOps {

  import NewUserMessageCodec._

  def toMessage(txt: String): Message = {
    val res = for {
      ws <- decode[IncomeMessage](txt)
      msg <- getActualMessage(ws)
    } yield msg
    res match {
      case Left(v) => throw v //TODO log exception
      case Right(v) => v
    }
  }

  private def getActualMessage(ws: IncomeMessage) = ws match {
    case x if x.tpe == "empty" => decode[EmptyMessage](x.body)
    case x if x.tpe == "new_user" => decode[NewUserMessage](x.body)
    case x if x.tpe == "layer_up" => decode[LayerUpMessage](x.body)
    case x if x.tpe == "resource_claim" => decode[ResourceClaimMessage](x.body)
    case x if x.tpe == "building_construction" => decode[BuildingConstructionMessage](x.body)
    case x if x.tpe == "add_production" => decode[AddProductionMessage](x.body)
    case x if x.tpe == "create_soul" => decode[CreateSoulMessage](x.body)
    case x if x.tpe == "create_troop" => decode[CreateTroopMessage](x.body)
    case x => Left(ParsingFailure(s"No converter for message ${x.tpe}", new RuntimeException()))
  }

  def toJson[A: Encoder](param: A): Json = param.asJson
}

object NewUserMessageCodec {
  implicit val decodeMessage: Decoder[NewUserMessage] = Decoder.forProduct2("name", "layer")(withGeneratedId)

  implicit val encodeMessage: Encoder[NewUserMessage] = Encoder.forProduct3("id", "name", "layer")(u => (u.user, u.name, u.layer))

  private def withGeneratedId(name: String, layer: String): NewUserMessage =
    NewUserMessage(UserIdGenerator.next, name, layer)
}