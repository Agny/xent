package ru.agny.xent.trade.persistence.slick

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}

import ru.agny.xent.core.inventory.{Item, ItemStack}
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.slick.{ItemRepository, UserRepository}
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

  override protected def beforeAll(): Unit = {
    MarketInitializer.forConfig(DbConfig.path).init()
    items.create(referenceItem)

    userId = Await.result(users.create("Test"), 1 seconds)
  }

  override protected def afterAll(): Unit = {
    items.delete(referenceItem.id)
    users.delete(userId)
  }

  "LotRepository" should "create lot record" in {
    val lotPlacement = PlaceLot(userId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(1, referenceItem.id, 1)), 1005000, None, Dealer.`type`)
    val res = repository.create(lotPlacement)
    res map { l =>
      l.id shouldNot be(None)
    }
  }

  it should "update lot's bid" in {
    val lotPlacement = PlaceLot(userId, ItemStack(1, referenceItem.id, 1), Price(ItemStack(1, referenceItem.id, 1)), 1005000, None, NonStrict.`type`)
    val bid = Bid(userId, Price(ItemStack(1, referenceItem.id, 1)))
    val updatedLot = for {
      lot <- repository.create(lotPlacement)
      _ <- repository.updateBid(lot.id.get, bid)
      withUpdatedBid <- repository.read(lot.id.get)
    } yield withUpdatedBid

    updatedLot map {
      case Some(v) if v.tpe == NonStrict.`type` =>
        v.user should be(userId)
        v.asInstanceOf[NonStrict].lastBid should be(Some(bid))
      case _ => fail("update failure")
    }
  }

}
