package ru.agny.xent.trade

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.javadsl.server.PathMatchers
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.{HttpEntity, StatusCodes, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.TransferEncodings.gzip
import akka.http.scaladsl.model.headers.{HttpEncoding, HttpEncodings}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import ru.agny.xent.core.inventory.ItemStack

import scala.io.StdIn
import scala.util.Random

object ApiServer extends App {

  import ru.agny.xent.trade.LotProtocol._

  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    .withContentType(ContentType(MediaTypes.`application/vnd.api+json`))
    .withParallelMarshalling(parallelism = 10, unordered = false)

  def fetchLots(): Source[Lot, NotUsed] = Source.fromIterator(() ⇒ Iterator.fill(10000) {
    val id = Random.nextLong()
    Strict(id, id, ItemStack(1, 1, 1), Price(ItemStack(2, 2, 2)), System.currentTimeMillis())
  })

  val route =
    pathPrefix("lots" / Remaining) { layer: String =>
      get {
        complete(fetchLots())
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
}
