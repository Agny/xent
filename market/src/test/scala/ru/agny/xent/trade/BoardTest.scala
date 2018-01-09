package ru.agny.xent.trade

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, BeforeAndAfterEach, Matchers}
import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.{ResponseFailure, ResponseOk}
import ru.agny.xent.persistence.slick.{DbConfig, ItemRepository, ItemStackRepository, UserRepository}
import ru.agny.xent.trade.Board._
import ru.agny.xent.trade.Lot.LotId
import ru.agny.xent.trade.persistence.slick.{BidRepository, LotRepository, MarketInitializer, ReservedItemRepository}
import ru.agny.xent.web.IncomeMessage

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Success

class BoardTest extends AsyncFlatSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {

  val underTest = Board("layer", DbConfig.path)
  implicit val stubRequestBuilder = StubRequestBuilder

  val lots = LotRepository(DbConfig.path)
  val items = ItemRepository(DbConfig.path)
  val stacks = ItemStackRepository(DbConfig.path)
  val users = UserRepository(DbConfig.path)
  val reserves = ReservedItemRepository(DbConfig.path)
  val bids = BidRepository(DbConfig.path)

  val referenceItem = new Item {
    val id = 1
    val weight = 1
  }
  val unspendableItemStack = ItemHolder(referenceItem.id, 100)
  val unreceivableItemStack = ItemHolder(referenceItem.id, 22)
  val unreceivable3 = ItemHolder(referenceItem.id, 3)
  val unreceivable4 = ItemHolder(referenceItem.id, 4)
  val unreceivable5 = ItemHolder(referenceItem.id, 5)
  val unreceivables = Seq(
    unreceivableItemStack,
    unreceivable3,
    unreceivable4,
    unreceivable5
  )
  var user1: UserId = _
  var user2: UserId = _
  var user3: UserId = _
  var user4: UserId = _

  override protected def beforeAll(): Unit = {
    MarketInitializer.forConfig(DbConfig.path).init()
    items.create(referenceItem)
    user1 = Await.result(users.create("Test"), 1 seconds)
    user2 = Await.result(users.create("Test"), 1 seconds)
    user3 = Await.result(users.create("Test"), 1 seconds)
    user4 = Await.result(users.create("Test"), 1 seconds)
  }

  override protected def afterEach(): Unit = {
    reserves.clean()
    bids.clean()
    stacks.clean()
  }

  override protected def afterAll() = {
    items.delete(referenceItem.id)
    users.delete(user1)
    users.delete(user2)
    users.delete(user3)
    users.delete(user4)
  }

  "Board.offer(Add)" should "add lot" in {
    val command = PlaceLot(user1, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 2), None, 1000, Dealer.`type`)
    val msg = Add(command)
    val result = for {
      res <- underTest.offer(msg)
      lots <- lots.findByUser(user1)
    } yield (res, lots)

    result.map {
      case (wsRes, lotsRes) =>
        wsRes should be(ResponseOk)
        lotsRes.size should be(1)
    }
  }

  it should "not add lot if operation is not verified" in {
    val cantBeVerified = PlaceLot(user1, unspendableItemStack, ItemHolder(referenceItem.id, 2), None, 1000, Dealer.`type`)
    val msg = Add(cantBeVerified)
    val result = for {
      res <- underTest.offer(msg)
      lots <- lots.findByUser(user1)
    } yield (res, lots)

    result.map {
      case (wsRes, lotsRes) =>
        wsRes should be(ResponseFailure)
        lotsRes.size should be(0)
    }
  }

  "Board.offer(Buy)" should "throw exception if lot doesn't exist" in {
    val msg = Buy(-1, user1)
    recoverToSucceededIf[IllegalStateException](underTest.offer(msg))
  }

  it should "delete lot if buy operation succeeded" in {
    val price = ItemHolder(referenceItem.id, 2)
    val placeLot = PlaceLot(user1, ItemHolder(referenceItem.id, 1), price, None, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val buy = (lotId: LotId) => Buy(lotId, user2)
    val result = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(user1)
      buyRes <- underTest.offer(buy(lot.head.id))
      lotEmpty <- lots.findByUser(user1)
      reserve <- reserves.findByUser(user1)
    } yield (buyRes, lotEmpty, reserve)

    result.map {
      case (response, emptyLots, emptyReserve) =>
        response should be(ResponseOk)
        emptyLots should be(Seq.empty)
        emptyReserve should be(Seq.empty)
    }
  }

  it should "reserve items if send transaction doesn't complete successfully" in {
    val placeLot = PlaceLot(user1, unreceivableItemStack, unreceivableItemStack, None, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val buy = (lotId: LotId) => Buy(lotId, user2)
    val result = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(user1)
      buyRes <- underTest.offer(buy(lot.head.id))
      lotEmpty <- lots.findByUser(user1)
      reserve1 <- {
        Thread.sleep(100) // wait for reserved items cleanup
        reserves.findByUser(user1)
      }
      reserve2 <- reserves.findByUser(user2)
    } yield (buyRes, lotEmpty, reserve1, reserve2)

    result.map {
      case (response, emptyLots, sellerReserve, buyerReserve) =>
        response should be(ResponseOk)
        emptyLots should be(Seq.empty)
        sellerReserve.size should be(1)
        buyerReserve.size should be(1)
    }
  }

  it should "handle simultaneous buy attempts" in {
    val price = unreceivableItemStack
    val placeLot = PlaceLot(user1, price, price, None, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val lotId = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(user1)
    } yield lot.head.id

    val tries = lotId.map(x => {
      val buy = Buy(x, user2)
      val firstTry = underTest.offer(buy)
      val secondTry = underTest.offer(buy)
      val thirdTry = underTest.offer(buy)
      Seq(firstTry, secondTry, thirdTry)
    })

    val successCount = tries.flatMap {
      _.foldLeft(Future.successful(0))((a, b) =>
        b.transform {
          case Success(_) => Success(a.value.get.get + 1)
          case _ => Success(a.value.get.get)
        })
    }

    val reservedItems = tries.transformWith(_ => {
      Thread.sleep(200) // wait for reserved items generation
      for {
        sold <- reserves.findByUser(user1)
        bought <- reserves.findByUser(user2)
      } yield (sold, bought)
    })

    (reservedItems zip successCount) map {
      case ((sold, bought), count) =>
        sold.size should be(1)
        bought.size should be(count)
    }
  }

  "Board.offer(PlaceBid)" should "update bid" in {
    val minPrice = ItemHolder(referenceItem.id, 2)
    val placeLot = PlaceLot(user1, ItemHolder(referenceItem.id, 1), minPrice, None, 1000, NonStrict.`type`)
    val bid = Bid(user2, minPrice)
    val add = Add(placeLot)
    val placeBid = (lotId: LotId) => PlaceBid(lotId, bid)
    val result = for {
      _ <- underTest.offer(add)
      added <- lots.findByUser(user1)
      res <- underTest.offer(placeBid(added.head.id))
      withBid <- lots.findByUser(user1)
    } yield (res, added.head, withBid.head)

    result.map {
      case (response, bidless: NonStrict, bidded: NonStrict) =>
        bidless.lastBid should be(None)
        bidded.lastBid should be(Some(bid))
        response should be(ResponseOk)
    }
  }

  it should "revert bid to None if verification fails" in {
    val placeLot = PlaceLot(user1, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 2), None, 1000, NonStrict.`type`)
    val bid = Bid(user2, unspendableItemStack)
    val add = Add(placeLot)
    val placeBid = (lotId: LotId) => PlaceBid(lotId, bid)
    val result = for {
      _ <- underTest.offer(add)
      added <- lots.findByUser(user1)
      res <- underTest.offer(placeBid(added.head.id))
      withBid <- lots.findByUser(user1)
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
    val placeLot = PlaceLot(user1, ItemHolder(referenceItem.id, 1), minPrice, None, 1000, NonStrict.`type`)
    val bid1 = Bid(user2, minPrice)
    val bid2 = Bid(user3, unspendableItemStack)
    val add = Add(placeLot)
    val placeBid = (lotId: LotId) => PlaceBid(lotId, bid1)
    val placeUnverifiable = (lotId: LotId) => PlaceBid(lotId, bid2)

    val result = for {
      _ <- underTest.offer(add)
      added <- lots.findByUser(user1)
      _ <- underTest.offer(placeBid(added.head.id))
      withBid <- lots.findByUser(user1)
      notVerified <- underTest.offer(placeUnverifiable(added.head.id))
      withPrevBid <- lots.findByUser(user1)
    } yield (notVerified, withBid.head, withPrevBid.head)

    result.map {
      case (response, bid: NonStrict, sameBid: NonStrict) =>
        bid should be(sameBid)
        response should be(ResponseFailure)
    }
  }

  it should "handle simultaneous bid attempts" in {
    val price = unreceivableItemStack
    val placeLot = PlaceLot(user1, price, price, Some(ItemHolder(referenceItem.id, 1)), 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val lotId = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(user1)
      _ <- underTest.offer(PlaceBid(lot.head.id, Bid(user2, ItemHolder(referenceItem.id, 2))))
      _ <- lots.findByUser(user1)
    } yield lot.head.id

    val tries = lotId.map(x => {
      val first = underTest.offer(PlaceBid(x, Bid(user3, unreceivable4)))
      val second = underTest.offer(PlaceBid(x, Bid(user3, unreceivable3)))
      val third = underTest.offer(PlaceBid(x, Bid(user4, unreceivable5)))
      Seq(first, second, third)
    })

    // depending on the order and timespace of incoming messages there can be from 1 to [tries.size] succeed tries
    val succeedTriesCounter = tries.flatMap(
      Future.traverse(_)(s => s.transform {
        case Success(_) => Success(1)
        case _ => Success(0)
      }).map(_.sum)
    )

    // when bid overwritten it's content is sent to owner
    // counting this records let us know how many bids was actually replaced
    val reservedItems = succeedTriesCounter.flatMap { x =>
      Thread.sleep(200) // wait for reserved items generation
      for {
        r <- reserves.load()
      } yield (r, x)
    }

    reservedItems map {
      case (reserved, count) =>
        reserved.size should be(count - 1)
    }
  }

  "Board.offer(Sell)" should "update lot amount remained" in {
    val price = ItemHolder(referenceItem.id, 2)
    val placeLot = PlaceLot(user1, ItemHolder(referenceItem.id, 2), price, None, 1000, Dealer.`type`)
    val sell = (lotId: LotId) => Sell(lotId, 1, user2)
    val add = Add(placeLot)
    val result = for {
      _ <- underTest.offer(add)
      added <- lots.findByUser(user1)
      res <- underTest.offer(sell(added.head.id))
      partiallySold <- lots.findByUser(user1)
    } yield (res, partiallySold.head)

    result.map {
      case (response, lot: Dealer) =>
        response should be(ResponseOk)
        lot.item.amount should be(1)
    }
  }

  it should "reserve items if send transaction doesn't complete successfully" in {
    val price = unreceivableItemStack
    val placeLot = PlaceLot(user1, ItemHolder(referenceItem.id, 2), price, None, 1000, Dealer.`type`)
    val sell = (lotId: LotId) => Sell(lotId, 1, user2)
    val add = Add(placeLot)
    val result = for {
      _ <- underTest.offer(add)
      added <- lots.findByUser(user1)
      res <- underTest.offer(sell(added.head.id))
      partiallySold <- lots.findByUser(user1)
      seller <- reserves.findByUser(user1)
    } yield (res, partiallySold.head, seller)

    result.map {
      case (response, lot: Dealer, sellerReserves) =>
        response should be(ResponseOk)
        lot.item.amount should be(1)
        sellerReserves.size should be(1)
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
        case Receive(_, s) if unreceivables.contains(s) => Future.successful(ResponseFailure)
        case Receive(_, _) => Future.successful(ResponseOk)
      }
    }
  }
}

