package ru.agny.xent.bench

import org.json4s.jackson.Serialization.{write => jacksonWrite}
import org.json4s.jackson.JsonMethods.{parse => jacksonParse}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import ru.agny.xent.persistence.MessageHandler
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.{decode => circeDecode}

@State(Scope.Benchmark)
class SerializationComparison {

  import org.json4s.DefaultFormats

  implicit val jacksonformats = DefaultFormats

  val data = generateValues(1000)
  val serializedCustom = data.map(_.toPersist)
  val serializedCirce = data.map(_.asJson.noSpaces)
  val serializedJackson = data.map(x => jacksonWrite(x))

  @Benchmark
  def customSerialize() = data.map(_.toPersist)

  @Benchmark
  def circeSerialize() = data.map(_.asJson.noSpaces)

  @Benchmark
  def jacksonSerialize() = data.map(x => jacksonWrite(x))

  @Benchmark
  def customDeserialize() = serializedCustom.map(x => MessageHandler.convert(x))

  @Benchmark
  def circeDeserialize() = serializedCirce.map(circeDecode[Foo])

  @Benchmark
  def jacksonDeserialize() = serializedJackson.map(jacksonParse(_).extract[Foo])

  private def generateValues(size: Int) = 1 to size map (i => Foo(i, Vector(i - 1, i, i + 1), i toString, Bar(s"name$i", i)))

}


