package ru.agny.xent.trade

import io.circe.generic.auto._
import io.circe.syntax._
import akka.serialization.SerializerWithStringManifest

class CirceSerializer extends SerializerWithStringManifest {

  import ru.agny.xent.trade.LotProtocol._

  override val identifier = 77
  private val boardState = "boardState"
  private val lot = "lot"

  override def toBinary(o: AnyRef) = o match {
    case l: Lot => l.asJson.noSpaces.toCharArray.map(_.toByte)
    case b: BoardState => b.asJson.noSpaces.toCharArray.map(_.toByte)
    case _ => Array.empty
  }

  override def manifest(o: AnyRef) = o match {
    case _: Lot => lot
    case _: BoardState => boardState
  }

  override def fromBinary(bytes: Array[Byte], manifest: String) = manifest match {
    case v if v == lot => String.valueOf(bytes.map(_.toChar)).asJson.as[Lot]
    case v if v == boardState => String.valueOf(bytes.map(_.toChar)).asJson.as[BoardState]
    case _ => bytes
  }

}
