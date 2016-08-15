package ru.agny.xent.web.utils

import java.util.concurrent.atomic.AtomicLong
import ru.agny.xent._
import org.json4s._
import org.json4s.jackson.JsonMethods._

object JsonOps {

  private implicit val formats = DefaultFormats
  private val idGen = new AtomicLong(0)

  def toMessage(txt: String): Message = {
    parse(txt).extract[WSMessage] match {
      case x if x.tpe == "empty" => parse(x.body).extract[EmptyMessage]
      case x if x.tpe == "new_user" => parse(x.body).merge(JObject("user" -> JLong(idGen.incrementAndGet()))).extract[NewUserMessage]
      case x if x.tpe == "layer_up" => parse(x.body).extract[LayerUpMessage]
      case x if x.tpe == "resource_claim" => parse(x.body).extract[ResourceClaimMessage]
      case x if x.tpe == "building_construction" => parse(x.body).extract[BuildingConstructionMessage]
      case x if x.tpe == "add_production" => parse(x.body).extract[AddProductionMessage]
      case x => throw new UnsupportedOperationException(s"No converter for message ${x.tpe}")
    }
  }
}

case class WSMessage(tpe: String, body: String)
