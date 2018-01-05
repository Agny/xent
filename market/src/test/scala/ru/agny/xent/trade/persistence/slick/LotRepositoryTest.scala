package ru.agny.xent.trade.persistence.slick

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}
import ru.agny.xent.core.inventory.{Item, ItemStack}
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
    val lotPlacement = PlaceLot(userId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(1, referenceItem.id, 1)), 1005000, Dealer.`type`)
    val res = repository.create(lotPlacement)
    res map { l =>
      l should not be (-1)
    }
  }

  it should "return all user lots" in {
    val userLot1 = PlaceLot(specifiedUserId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(1, referenceItem.id, 1)), 1005000, Dealer.`type`)
    val userLot2 = PlaceLot(specifiedUserId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(1, referenceItem.id, 1)), 1005000, Dealer.`type`)
    val otherLot = PlaceLot(otherUserId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(1, referenceItem.id, 1)), 1005000, Dealer.`type`)
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

  it should "create itemstack and buyout entities along with lot" in {
    val itemToSell = ItemStack(1, referenceItem.id, 1)
    val buyout = Price(ItemStack(1, referenceItem.id, 1))
    val lotPlacement = PlaceLot(userId, itemToSell, buyout, 1005000, NonStrict.`type`)
    val result = for {
      lot <- repository.create(lotPlacement)
      fullLoaded <- repository.read(lot)
    } yield fullLoaded

    result map { mbLot =>
      mbLot shouldNot be(None)
      val lot = mbLot.get
      lot.buyout should be(buyout)
      lot.item should be(itemToSell)
    }
  }

  it should "update lot's bid" in {
    val lotPlacement = PlaceLot(userId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(1, referenceItem.id, 1)), 1005000, NonStrict.`type`)
    val bid = Bid(userId, Price(ItemStack(1, referenceItem.id, 1)))
    val updatedLot = for {
      lot <- repository.create(lotPlacement)
      _ <- repository.updateBid(lot, bid)
      withUpdatedBid <- repository.read(lot)
    } yield withUpdatedBid

    updatedLot map {
      case Some(v) if v.tpe == NonStrict.`type` =>
        v.user should be(userId)
        v.asInstanceOf[NonStrict].lastBid.get.price should be(bid.price)
      case _ => fail("update failure")
    }
  }

  it should "fail to update bid by less price than lot's bid has" in {
    val lotPlacement = PlaceLot(userId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(5, referenceItem.id, 1)), 1005000, NonStrict.`type`)
    val bid = Bid(userId, Price(ItemStack(3, referenceItem.id, 1)))
    val smallerBid = Bid(otherUserId, Price(ItemStack(2, referenceItem.id, 1)))
    val updatedLot = for {
      lot <- repository.create(lotPlacement)
      _ <- repository.updateBid(lot, bid)
      shouldFailHere <- repository.updateBid(lot, smallerBid)
      withUpdatedBid <- repository.read(lot)
    } yield withUpdatedBid

    recoverToSucceededIf[IllegalStateException](updatedLot)
  }

  it should "return lot when buying" in {
    val itemToSell = ItemStack(5, referenceItem.id, 1)
    val lotPlacement = PlaceLot(userId, ItemStack(1, referenceItem.id, 1), Price(itemToSell), 1005000, NonStrict.`type`)
    val buyoutBid = Bid(otherUserId, Price(ItemStack(5, referenceItem.id, 1)))
    val result = for {
      lot <- repository.create(lotPlacement)
      loaded <- repository.buy(lot, buyoutBid)
    } yield (lot, loaded)

    result map { case (lotId, toBuy) =>
      lotId should be(toBuy.id)
      toBuy.buyout.amount should be(itemToSell)
    }
  }

  it should "fail to buy lot by less price than lot's buyout" in {
    val lotPlacement = PlaceLot(userId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(5, referenceItem.id, 1)), 1005000, NonStrict.`type`)
    val notHighEnoughBid = Bid(userId, Price(ItemStack(4, referenceItem.id, 1)))
    val result = for {
      lot <- repository.create(lotPlacement)
      rowsAffected <- repository.buy(lot, notHighEnoughBid)
      lotNone <- repository.read(lot)
    } yield (rowsAffected, lotNone)

    recoverToSucceededIf[IllegalStateException](result)
  }

}
