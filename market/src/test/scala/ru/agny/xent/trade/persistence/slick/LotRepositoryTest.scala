package ru.agny.xent.trade.persistence.slick

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.trade.{Dealer, Price}

class LotRepositoryTest extends AsyncFlatSpec with Matchers with BeforeAndAfterAll {

  val repository = LotRepository(DbConfig.path)

  override protected def beforeAll(): Unit = {
    MarketInitializer.forConfig(DbConfig.path).init()
  }

  override protected def afterAll(): Unit = {
  }

  "LotRepository" should "create lot record" in {
    val id = 1
    val lot = Dealer(id, 1, ItemStack(1, 1, 1), Price(ItemStack(1, 1, 1)), 1005000)
    val res = repository.create(lot)
    res map { l =>
      l.id should be(Some(id))
    }
  }

}
