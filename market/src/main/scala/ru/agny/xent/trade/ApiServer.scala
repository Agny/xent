package ru.agny.xent.trade

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import ru.agny.xent.trade.Board._
import ru.agny.xent.trade.persistence.slick.MarketInitializer

import scala.io.StdIn

object ApiServer extends LazyLogging {

  import ru.agny.xent.trade.BidProtocol._
  import ru.agny.xent.trade.LotProtocol._

  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    .withContentType(ct)
    .withParallelMarshalling(parallelism = 10, unordered = false)

  val dbPath = "db"
  val initDB = {
    logger.debug("Initializing database tables...")
    val initializer = MarketInitializer.forConfig(dbPath)
    initializer.init()
    initializer
  }

  //  val serverContext: ConnectionContext = {
  //    val password = "abcdef".toCharArray
  //    val context = SSLContext.getInstance("TLS")
  //    val ks = KeyStore.getInstance("PKCS12")
  //    ks.load(getClass.getClassLoader.getResourceAsStream("keys/server.p12"), password)
  //    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
  //    keyManagerFactory.init(ks, password)
  //    context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)
  //    // start up the web server
  //    ConnectionContext.https(context)
  //  }

  val layerId = "layer_1"
  val board = Board(layerId, dbPath)

  val route = pathPrefix("market") {
    path("lots" / Remaining) { layer: String =>
      get {
        complete(board.lots())
      }
    } ~ path("place") {
      post {
        entity(as[PlaceLot]) { lot =>
          complete(board.offer(Add(lot)))
        }
      }
    } ~ path("bid" / LongNumber) { lotId: Long =>
      post {
        entity(as[Bid]) { bid =>
          complete(board.offer(PlaceBid(lotId, bid)))
        }
      }
    } ~ path("buy" / LongNumber) { lotId: Long =>
      post {
        entity(as[Bid]) { bid =>
          complete(board.offer(Buy(lotId, bid)))
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8888)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
  }
}
