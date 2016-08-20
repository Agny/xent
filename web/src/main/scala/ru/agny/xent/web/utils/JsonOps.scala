package ru.agny.xent.web.utils

import java.util.concurrent.atomic.AtomicLong
import ru.agny.xent._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import ru.agny.xent.core.WorldCell

object JsonOps {

  private implicit val formats = DefaultFormats + WorldCellSerializer + VectorSerializer
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

  def fromLayer(layer: Layer): String = {
    write(layer.map.view)
  }

  case class WSMessage(tpe: String, body: String)
}

/**
  * <code>type UserId = Long</code> can't be serialized
  * @see <a href="https://github.com/json4s/json4s/issues/76">json4s/issues/76</a>
  *
  */
object WorldCellSerializer extends Serializer[WorldCell] {
  val WorldCellClass = classOf[WorldCell]

  override def deserialize(implicit format: Formats) = ??? //don't care

  override def serialize(implicit format: Formats) = {
    case cell: WorldCell =>
      val jj = (("x" -> JInt(cell.x))
        :: ("y" -> JInt(cell.y))
        :: ("resource" -> Extraction.decompose(cell.resource))
        :: ("city" -> Extraction.decompose(cell.city))
        :: ("owner" -> Extraction.decompose(cell.owner.map(_.toLong))) :: Nil)
      JObject(jj)

  }
}

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
