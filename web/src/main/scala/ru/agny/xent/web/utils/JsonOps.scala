package ru.agny.xent.web.utils

import java.util.concurrent.atomic.AtomicLong

import org.json4s._
import ru.agny.xent.core.unit.Characteristic
import ru.agny.xent.messages._
import ru.agny.xent.messages.production._
import ru.agny.xent.messages.unit.{CreateSoulMessage, CreateTroopMessage, StatPropertySimple}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._

object JsonOps {

  private implicit val formats = DefaultFormats + VectorSerializer //+ StatPropertySimpleSerializer
  private val idGen = new AtomicLong(0)

  def toMessage(txt: String): Message = {
    parse(txt).extract[WSMessage] match {
      case x if x.tpe == "empty" => parse(x.body).extract[EmptyMessage]
      case x if x.tpe == "new_user" => parse(x.body).merge(JObject("user" -> JLong(idGen.incrementAndGet()))).extract[NewUserMessage]
      case x if x.tpe == "layer_up" => parse(x.body).extract[LayerUpMessage]
      case x if x.tpe == "resource_claim" => parse(x.body).extract[ResourceClaimMessage]
      case x if x.tpe == "building_construction" => parse(x.body).extract[BuildingConstructionMessage]
      case x if x.tpe == "add_production" => parse(x.body).extract[AddProductionMessage]
      case x if x.tpe == "create_soul" => parse(x.body).extract[CreateSoulMessage]
      case x if x.tpe == "create_troop" => parse(x.body).extract[CreateTroopMessage]
      case x => throw new UnsupportedOperationException(s"No converter for message ${x.tpe}")
    }
  }

  //TODO compact(render(fields)) with JsonDSL._
  def toJson(fields: Map[String, String]): JValue = {
    JObject(fields.toList.map { case (k, v) ⇒ JField(k, JString(v)) })
  }

  def toString(param: AnyRef): String = {
    write(param)
  }

  case class WSMessage(tpe: String, body: String)
}

/**
  * <code>type UserId = Long</code> can't be serialized
  *
  * @see <a href="https://github.com/json4s/json4s/issues/76">json4s/issues/76</a>
  *
  */
//object WorldCellSerializer extends Serializer[WorldCell] {
//  val WorldCellClass = classOf[WorldCell]
//
//  override def deserialize(implicit format: Formats) = ??? //don't care
//
//  override def serialize(implicit format: Formats) = {
//    case cell: WorldCell =>
//      val jj = (("x" -> JInt(cell.x))
//        :: ("y" -> JInt(cell.y))
//        :: ("resource" -> Extraction.decompose(cell.resource))
//        :: ("city" -> Extraction.decompose(cell.city))
//        :: ("owner" -> Extraction.decompose(cell.owner.map(_.toLong))) :: Nil)
//      JObject(jj)
//
//  }
//}

object VectorSerializer extends Serializer[Vector[_]] {
  val VectorClass = classOf[Vector[_]]

  override def deserialize(implicit format: Formats) = {
    case (TypeInfo(VectorClass, parameterizedType), JArray(xs)) =>
      val typeInfo = TypeInfo(parameterizedType
        .map(_.getActualTypeArguments()(0))
        .getOrElse(reflect.fail("No type parameter info for type Vector"))
        .asInstanceOf[Class[_]],
        None)
      xs.map(x => Extraction.extract(x, typeInfo)).toVector
  }

  override def serialize(implicit format: Formats) = {
    case vector: Vector[_] => JArray(vector.toList.map(Extraction.decompose))
  }
}

object StatPropertySimpleSerializer extends Serializer[StatPropertySimple] {
  val StatPropertySimpleClass = classOf[StatPropertySimple]

  override def deserialize(implicit format: Formats) = {
    case (TypeInfo(StatPropertySimpleClass, _), JObject(xs)) =>
      (for {
        prop <- xs.find(_._1 == "prop")
        ch <- Characteristic.from(prop._2.extract[String])
        level <- xs.find(_._1 == "level")
      } yield StatPropertySimple(prop._1, level._2.extract[Int])
        ) getOrElse (throw new UnsupportedOperationException(s"No Characteristic for ${xs.find(_._1 == "prop").get._2}"))

  }

  override def serialize(implicit format: Formats) = ??? //don't care
}
