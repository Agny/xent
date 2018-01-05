package ru.agny.xent.trade

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterEach, Matchers}
import ru.agny.xent.core.inventory.{Item, ItemStack}
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
  val unspendableItemStack = ItemStack(100, referenceItem.id, 20)
  val spendableWithCondition = ItemStack(100, referenceItem.id, 2000)
  val unreceivableItemStack = ItemStack(22, referenceItem.id, 20)
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
    val command = PlaceLot(userId, ItemStack(1, referenceItem.id, 20), Price(ItemStack(2, referenceItem.id, 20)), 1000, Dealer.`type`)
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
    val cantBeVerified = PlaceLot(userId, unspendableItemStack, Price(ItemStack(2, referenceItem.id, 20)), 1000, Dealer.`type`)
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
    val msg = Buy(-1, Bid(userId, Price(ItemStack(2, referenceItem.id, 20))))
    recoverToSucceededIf[IllegalStateException](underTest.offer(msg))
  }

  it should "throw exception if bid isn't high enough" in {
    val actualPrice = Price(ItemStack(2, referenceItem.id, 20))
    val biddedPrice = Price(ItemStack(1, referenceItem.id, 20))
    val placeLot = PlaceLot(userId, ItemStack(1, referenceItem.id, 20), actualPrice, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val buy = (lotId: LotId) => Buy(lotId, Bid(userId, biddedPrice))
    val result = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(userId)
      buyRes <- underTest.offer(buy(lot.head.id))
    } yield buyRes

    recoverToSucceededIf[IllegalStateException](result)
  }

  it should "not proceed buy operation if bid isn't verified" in {
    val actualPrice = Price(spendableWithCondition)
    val biddedPrice = Price(unspendableItemStack)
    val placeLot = PlaceLot(userId, ItemStack(1, referenceItem.id, 20), actualPrice, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val buy = (lotId: LotId) => Buy(lotId, Bid(userId, biddedPrice))
    val result = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(userId)
      buyRes <- underTest.offer(buy(lot.head.id))
      lotAgain <- lots.findByUser(userId)
    } yield (buyRes, lotAgain)

    result.map {
      case (response, lot) =>
        response should be(ResponseFailure)
        lot.size should be(1)
    }
  }

  it should "delete lot if buy operation succeeded" in {
    val price = Price(ItemStack(2, referenceItem.id, 20))
    val placeLot = PlaceLot(userId, ItemStack(1, referenceItem.id, 20), price, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val buy = (lotId: LotId) => Buy(lotId, Bid(userId, price))
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
    val price = Price(unreceivableItemStack)
    val placeLot = PlaceLot(userId, unreceivableItemStack, price, 1000, NonStrict.`type`)
    val add = Add(placeLot)
    val buy = (lotId: LotId) => Buy(lotId, Bid(otherUserId, price))
    val result = for {
      _ <- underTest.offer(add)
      lot <- lots.findByUser(userId)
      buyRes <- underTest.offer(buy(lot.head.id))
      lotEmpty <- lots.findByUser(userId)
      reserve1 <- reserves.findByUser(userId)
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
    val placeLot = PlaceLot(userId, ItemStack(1, referenceItem.id, 20), Price(ItemStack(2, referenceItem.id, 20)), 1000, NonStrict.`type`)
    val bid = Bid(otherUserId, Price(ItemStack(1, referenceItem.id, 20)))
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
    val placeLot = PlaceLot(userId, ItemStack(1, referenceItem.id, 20), Price(ItemStack(2, referenceItem.id, 20)), 1000, NonStrict.`type`)
    val bid = Bid(otherUserId, Price(unspendableItemStack))
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
    val placeLot = PlaceLot(userId, ItemStack(1, referenceItem.id, 20), Price(ItemStack(2, referenceItem.id, 20)), 1000, NonStrict.`type`)
    val bid1 = Bid(otherUserId, Price(ItemStack(1, referenceItem.id, 20)))
    val bid2 = Bid(otherUserId, Price(unspendableItemStack))
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

