package ru.agny.xent.trade

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.trade.Board.Add
import ru.agny.xent.trade.persistence.slick.MarketInitializer

import scala.io.StdIn

object ApiServer extends App {

  import ru.agny.xent.trade.LotProtocol._

  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    .withContentType(ct)
    .withParallelMarshalling(parallelism = 10, unordered = false)

  val initDB = {
    println("Initializing database tables...")
    MarketInitializer.init()
  }

  val layerId = "layer_1"
  val board = Board(layerId)

  val route = pathPrefix("market") {
    path("lots" / Remaining) { layer: String =>
      get {
        complete(board.lots())
      }
    } ~ path("place") {
      post {
        entity(as[Lot]) { lot =>
          board.offer(Add(lot))
          complete(board.lots())
        }
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
