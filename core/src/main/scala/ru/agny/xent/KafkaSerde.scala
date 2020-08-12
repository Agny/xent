package ru.agny.xent

import java.nio.charset.StandardCharsets

import cats.syntax.EitherOps
import io.circe.{Decoder, Encoder}
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.common.serialization.{Deserializer, Serializer}

object KafkaSerde {

  val unitSerializer: Serializer[Unit] = new Serializer[Unit] {
    override def serialize(topic: String, data: Unit): Array[Byte] = Array.empty
  } 
  val unitDeserializer: Deserializer[Unit] = new Deserializer[Unit] {
    override def deserialize(topic: String, data: Array[Byte]): Unit = ()
  }
    
  def serializer[A: Encoder](): Serializer[A] = new Serializer[A] {
    override def serialize(topic: String, data: A): Array[Byte] = {
      data.asJson.noSpaces.getBytes(StandardCharsets.UTF_8)
    }
  }
  def deserializer[A: Decoder](): Deserializer[A] = new Deserializer[A] {
    override def deserialize(topic: String, data: Array[Byte]): A = {
      val msg = new String(data, StandardCharsets.UTF_8)
      EitherOps(decode[A](msg)).valueOr(ex => throw new RuntimeException(s"""Could not parse value from "$msg".""", ex))
    }
  }
}
