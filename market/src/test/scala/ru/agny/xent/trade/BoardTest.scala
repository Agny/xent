package ru.agny.xent.trade

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterEach, Matchers}
import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.{ResponseFailure, ResponseOk}
import ru.agny.xent.persistence.slick.{DbConfig, ItemRepository, UserRepository}
import ru.agny.xent.trade.Board._
import ru.agny.xent.trade.Lot.LotId
import ru.agny.xent.trade.persistence.slick.{LotRepository, MarketInitializer, ReservedItemRepository}
import ru.agny.xent.web.IncomeMessage

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class BoardTest extends AsyncFlatSpec with Matchers with BeforeAndAfterEach {

  val underTest = Board("layer", DbConfig.path)
  implicit val stubRequestBuilder = StubRequestBuilder

  val lots = LotRepository(DbConfig.path)
  val items = ItemRepository(DbConfig.path)
  val users = UserRepository(DbConfig.path)
  val reserves = ReservedItemRepository(DbConfig.path)

  val referenceItem = new Item {
    val id = 1
    val weight = 1
  }
  val unspendableItemStack = ItemHolder(referenceItem.id, 100)
  val unreceivableItemStack = ItemHolder(referenceItem.id, 22)
  var userId: UserId = _
  var otherUserId: UserId = _

  override protected def beforeEach(): Unit = {
    MarketInitializer.forConfig(DbConfig.path).init()
    items.create(referenceItem)

    userId = Await.result(users.create("Test"), 1 seconds)
    otherUserId = Await.result(users.create("Test"), 1 seconds)
  }

  override protected def afterEach(): Unit = {
    items.delete(referenceItem.id)
    users.delete(userId)
    users.delete(otherUserId)
  }

  "Board.offer(Add)" should "add lot" in {
    val command = PlaceLot(userId, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 2), None, 1000, Dealer.`type`)
    val msg = Add(command)
    val result = for {
      res <- underTest.offer(msg)
      lots <- lots.findByUser(userId)
    } yield (res, lots)

    result.map {
      case (wsRes, lotsRes) =>
        wsRes should be(ResponseOk)
        lotsRes.size should be(1)
    }
  }

  it should "not add lot if operation is not verified" in {
    val cantBeVerified = PlaceLot(userId, unspendableItemStack, ItemHolder(referenceItem.id, 2), None, 1000, Dealer.`type`)
    val msg = Add(cantBeVerified)
    val result = for {
      res <- underTest.offer(msg)
      lots <- lots.findByUser(userId)
    } yield (res, lots)

    result.map {
      case (wsRes, lotsRes) =>
        wsRes should be(ResponseFailure)
        lotsRes.size should be(0)
    }
  }

  "Board.offer(Buy)" should "throw exception if lot doesn't exist" in {
    val msg = Buy(-1, userId)
    recoverToSucceededIf[IllegalStateException](underTest.offer(msg))
  }

  it should "delete lot if buy operation succeeded" in {
    val price = ItemHolder(referenceItem.id, 2)
    val placeLot = PlaceLot(userId, ItemHolder(referenceItem.id, 1), price, None, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val buy = (lotId: LotId) => Buy(lotId, otherUserId)
    val result = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(userId)
      buyRes <- underTest.offer(buy(lot.head.id))
      lotEmpty <- lots.findByUser(userId)
      reserve <- reserves.findByUser(userId)
    } yield (buyRes, lotEmpty, reserve)

    result.map {
      case (response, emptyLots, emptyReserve) =>
        response should be(ResponseOk)
        emptyLots should be(Seq.empty)
        emptyReserve should be(Seq.empty)
    }
  }

  it should "reserve items if send transaction doesn't complete successfully" in {
    val placeLot = PlaceLot(userId, unreceivableItemStack, unreceivableItemStack, None, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val buy = (lotId: LotId) => Buy(lotId, otherUserId)
    val result = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(userId)
      buyRes <- underTest.offer(buy(lot.head.id))
      lotEmpty <- lots.findByUser(userId)
      reserve1 <- {
        Thread.sleep(100) // wait for reserved items cleanup
        reserves.findByUser(userId)
      }
      reserve2 <- reserves.findByUser(otherUserId)
    } yield (buyRes, lotEmpty, reserve1, reserve2)

    result.map {
      case (response, emptyLots, sellerReserve, buyerReserve) =>
        response should be(ResponseOk)
        emptyLots should be(Seq.empty)
        sellerReserve.size should be(1)
        buyerReserve.size should be(1)
    }
  }

  "Board.offer(PlaceBid)" should "update bid" in {
    val minPrice = ItemHolder(referenceItem.id, 2)
    val placeLot = PlaceLot(userId, ItemHolder(referenceItem.id, 1), minPrice, None, 1000, NonStrict.`type`)
    val bid = Bid(otherUserId, minPrice)
    val add = Add(placeLot)
    val placeBid = (lotId: LotId) => PlaceBid(lotId, bid)
    val result = for {
      _ <- underTest.offer(add)
      added <- lots.findByUser(userId)
      res <- underTest.offer(placeBid(added.head.id))
      withBid <- lots.findByUser(userId)
    } yield (res, added.head, withBid.head)

    result.map {
      case (response, bidless: NonStrict, bidded: NonStrict) =>
        bidless.lastBid should be(None)
        bidded.lastBid should be(Some(bid))
        response should be(ResponseOk)
    }
  }

  it should "revert bid to None if verification fails" in {
    val placeLot = PlaceLot(userId, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 2), None, 1000, NonStrict.`type`)
    val bid = Bid(otherUserId, unspendableItemStack)
    val add = Add(placeLot)
    val placeBid = (lotId: LotId) => PlaceBid(lotId, bid)
    val result = for {
      _ <- underTest.offer(add)
      added <- lots.findByUser(userId)
      res <- underTest.offer(placeBid(added.head.id))
      withBid <- lots.findByUser(userId)
    } yield (res, added.head, withBid.head)

    result.map {
      case (response, bidless: NonStrict, bidded: NonStrict) =>
        bidless.lastBid should be(None)
        bidded.lastBid should be(None)
        response should be(ResponseFailure)
    }
  }

  it should "revert bid to previous value if verification fails " in {
    val minPrice = ItemHolder(referenceItem.id, 2)
    val placeLot = PlaceLot(userId, ItemHolder(referenceItem.id, 1), minPrice, None, 1000, NonStrict.`type`)
    val bid1 = Bid(otherUserId, minPrice)
    val bid2 = Bid(otherUserId, unspendableItemStack)
    val add = Add(placeLot)
    val placeBid = (lotId: LotId) => PlaceBid(lotId, bid1)
    val placeUnverifiable = (lotId: LotId) => PlaceBid(lotId, bid2)

    val result = for {
      _ <- underTest.offer(add)
      added <- lots.findByUser(userId)
      _ <- underTest.offer(placeBid(added.head.id))
      withBid <- lots.findByUser(userId)
      notVerified <- underTest.offer(placeUnverifiable(added.head.id))
      withPrevBid <- lots.findByUser(userId)
    } yield (notVerified, withBid.head, withPrevBid.head)

    result.map {
      case (response, bid: NonStrict, sameBid: NonStrict) =>
        bid should be(sameBid)
        response should be(ResponseFailure)
    }
  }

  case class StubWSRequest(in: IncomeMessage) extends WSRequest {
    override lazy val out = ???
  }

  object StubRequestBuilder extends WSAdapter[StubWSRequest] {
    override def build(msg: IncomeMessage) = ???

    override def send(tpe: String, msg: Board.ItemCommand) = {
      msg match {
        case Spend(_, s) if s == unspendableItemStack => Future.successful(ResponseFailure)
        case Spend(_, _) => Future.successful(ResponseOk)
        case Receive(_, s) if s == unreceivableItemStack => Future.successful(ResponseFailure)
        case Receive(_, _) => Future.successful(ResponseOk)
      }
    }
  }
}

