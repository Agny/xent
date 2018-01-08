package ru.agny.xent.trade.persistence.slick

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}
import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.{DbConfig, ItemRepository, UserRepository}
import ru.agny.xent.trade._

import scala.concurrent.Await
import scala.concurrent.duration._

class LotRepositoryTest extends AsyncFlatSpec with Matchers with BeforeAndAfterAll {

  val repository = LotRepository(DbConfig.path)
  val items = ItemRepository(DbConfig.path)
  val users = UserRepository(DbConfig.path)

  val referenceItem = new Item {
    val id = 1
    val weight = 1
  }
  var userId: UserId = _
  var specifiedUserId: UserId = _
  var otherUserId: UserId = _

  override protected def beforeAll(): Unit = {
    MarketInitializer.forConfig(DbConfig.path).init()
    items.create(referenceItem)

    userId = Await.result(users.create("Test"), 1 seconds)
    specifiedUserId = Await.result(users.create("Test2"), 1 seconds)
    otherUserId = Await.result(users.create("Test3"), 1 seconds)
  }

  override protected def afterAll(): Unit = {
    items.delete(referenceItem.id)
    users.delete(userId)
    users.delete(specifiedUserId)
    users.delete(otherUserId)
  }

  "LotRepository" should "create lot record" in {
    val lotPlacement = PlaceLot(userId, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 1), None, 1005000, Dealer.`type`)
    val res = repository.create(lotPlacement)
    res map { l =>
      l should not be (-1)
    }
  }

  it should "return all user lots" in {
    val userLot1 = PlaceLot(specifiedUserId, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 1), None, 1005000, Dealer.`type`)
    val userLot2 = PlaceLot(specifiedUserId, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 1), None, 1005000, Dealer.`type`)
    val otherLot = PlaceLot(otherUserId, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 1), None, 1005000, Dealer.`type`)
    val result = for {
      _ <- repository.create(userLot1)
      _ <- repository.create(userLot2)
      _ <- repository.create(otherLot)
      res <- repository.findByUser(specifiedUserId)
    } yield res

    result map { x =>
      x.size should be(2)
    }
  }

  it should "create ItemHolder and buyout entities along with lot" in {
    val item = ItemHolder(referenceItem.id, 1)
    val lotPlacement = PlaceLot(userId, item, item, None, 1005000, NonStrict.`type`)
    val result = for {
      lot <- repository.create(lotPlacement)
      fullLoaded <- repository.read(lot)
    } yield fullLoaded

    result map { mbLot =>
      mbLot shouldNot be(None)
      val lot = mbLot.get
      lot.buyout should be(item)
      lot.item should be(item)
    }
  }

  it should "update lot's bid" in {
    val item = ItemHolder(referenceItem.id, 1)
    val lotPlacement = PlaceLot(userId, item, item, None, 1005000, NonStrict.`type`)
    val bid = Bid(otherUserId, item)
    val updatedLot = for {
      lot <- repository.create(lotPlacement)
      _ <- repository.insertBid(lot, bid)
      withUpdatedBid <- repository.read(lot)
    } yield withUpdatedBid

    updatedLot map {
      case Some(v) if v.tpe == NonStrict.`type` =>
        v.user should be(userId)
        v.asInstanceOf[NonStrict].lastBid.get.price should be(bid.price)
      case _ => fail("update failure")
    }
  }

  it should "fail to prepare bid update by less price than lot's bid has" in {
    val lotPlacement = PlaceLot(userId, ItemHolder(referenceItem.id, 1), ItemHolder(referenceItem.id, 5), None, 1005000, NonStrict.`type`)
    val bid = Bid(userId, ItemHolder(referenceItem.id, 3))
    val smallerBid = Bid(otherUserId, ItemHolder(referenceItem.id, 2))
    val updatedLot = for {
      lot <- repository.create(lotPlacement)
      failHere <- repository.biddingPreparement(lot, bid)
      _ <- repository.insertBid(lot, smallerBid)
      withUpdatedBid <- repository.read(lot)
    } yield withUpdatedBid

    recoverToSucceededIf[IllegalStateException](updatedLot)
  }

  it should "return lot when buying" in {
    val itemToSell = ItemHolder(referenceItem.id, 5)
    val lotPlacement = PlaceLot(userId, ItemHolder(referenceItem.id, 1), itemToSell, None, 1005000, NonStrict.`type`)
    val result = for {
      lot <- repository.create(lotPlacement)
      loaded <- repository.buyPreparement(lot, otherUserId)
    } yield (lot, loaded)

    result map { case (lotId, toBuy) =>
      lotId should be(toBuy.id)
      toBuy.buyout should be(itemToSell)
    }
  }

  it should "not let user to bid on his own lot" in {
    val item = ItemHolder(referenceItem.id, 1)
    val lotPlacement = PlaceLot(userId, item, item, None, 1005000, NonStrict.`type`)
    val bid = Bid(userId, item)
    val updatedLot = for {
      lot <- repository.create(lotPlacement)
      failHere <- repository.biddingPreparement(lot, bid)
      withUpdatedBid <- repository.read(lot)
    } yield withUpdatedBid

    recoverToSucceededIf[IllegalStateException](updatedLot)
  }

  it should "not let user to buy his own lot" in {
    val item = ItemHolder(referenceItem.id, 1)
    val lotPlacement = PlaceLot(userId, item, item, None, 1005000, NonStrict.`type`)
    val bid = Bid(userId, item)
    val result = for {
      lot <- repository.create(lotPlacement)
      failHere <- repository.buyPreparement(lot, userId)
    } yield failHere

    recoverToSucceededIf[IllegalStateException](result)
  }

  it should "sell specified amount of Dealer lot" in {
    val item = ItemHolder(referenceItem.id, 5)
    val price = ItemHolder(referenceItem.id, 8)
    val toSell = 2
    val lotPlacement = PlaceLot(userId, item, price, None, 1005000, Dealer.`type`)
    val updated = for {
      lot <- repository.create(lotPlacement)
      sold <- repository.sell(lot, otherUserId, toSell)
      updated <- repository.read(lot)
    } yield (sold, updated)

    updated map {
      case (soldAmount, Some(lot)) =>
        soldAmount should be(toSell)
        lot.item should be(ItemHolder(item.id, item.amount - soldAmount))
    }
  }

  it should "sell all items if specified amount is greater than current amount" in {
    val item = ItemHolder(referenceItem.id, 5)
    val price = ItemHolder(referenceItem.id, 8)
    val toSell = 10
    val lotPlacement = PlaceLot(userId, item, price, None, 1005000, Dealer.`type`)
    val updated = for {
      lot <- repository.create(lotPlacement)
      sold <- repository.sell(lot, otherUserId, toSell)
      updated <- repository.read(lot)
    } yield (sold, updated)

    updated map {
      case (soldAmount, Some(lot)) =>
        soldAmount should be(item.amount)
        lot.item should be(ItemHolder(item.id, 0))
    }
  }

  it should "not sell already sold items" in {
    val item = ItemHolder(referenceItem.id, 5)
    val price = ItemHolder(referenceItem.id, 8)
    val toSell = item.amount
    val lotPlacement = PlaceLot(userId, item, price, None, 1005000, Dealer.`type`)
    val updated = for {
      lot <- repository.create(lotPlacement)
      sold <- repository.sell(lot, otherUserId, toSell)
      zero <- repository.sell(lot, otherUserId, toSell)
      updated <- repository.read(lot)
    } yield (sold, zero, updated)

    updated map {
      case (soldAmount, zero, Some(lot)) =>
        soldAmount should be(item.amount)
        zero should be(0)
        lot.item should be(ItemHolder(item.id, 0))
    }
  }

}
