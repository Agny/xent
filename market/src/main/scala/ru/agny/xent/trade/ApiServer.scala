package ru.agny.xent.trade

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.trade.Board.Add

import scala.io.StdIn
import scala.util.Random

object ApiServer extends App {

  import ru.agny.xent.trade.LotProtocol._

  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    .withContentType(ct)
    .withParallelMarshalling(parallelism = 10, unordered = false)

  val layerId = "layer_1"
  val board = Board(layerId)

  val route = pathPrefix("market") {
    path("lots" / Remaining) { layer: String =>
      get {
        complete(board.lots())
      }
    } ~ path("place") {
      get {
        board.offer(Add(NonStrict(Random.nextLong(), 1, ItemStack(1, 1, 1), Price(ItemStack(2, 2, 2)), 1000, None)))
        complete(board.lots())
      }
    } ~ path("bid" / LongNumber) { id: ItemId =>
      post {
        ???
      }
    } ~ path("buy" / LongNumber) { id: ItemId =>
      post {
        ???
      }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
}
