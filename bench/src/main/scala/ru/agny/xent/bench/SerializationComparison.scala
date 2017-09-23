package ru.agny.xent.bench

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import ru.agny.xent.persistence.MessageHandler
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

@State(Scope.Benchmark)
class SerializationComparison {

  val data = generateValues(1000)
  val serializedCustom = data.map(_.toPersist)
  val serializedCirce = data.map(_.asJson.noSpaces)

  @Benchmark
  def customSerialize() = data.map(_.toPersist)

  @Benchmark
  def circeSerialize() = data.map(_.asJson.noSpaces)

  @Benchmark
  def customDeserialize() = serializedCustom.map(x => MessageHandler.convert(x))

  @Benchmark
  def circeDeserialize() = serializedCirce.map(x => decode[Foo](x))

  private def generateValues(size: Int) = 1 to size map (i => Foo(i, Vector(i - 1, i, i + 1), i toString, Bar(s"name$i", i)))

}


