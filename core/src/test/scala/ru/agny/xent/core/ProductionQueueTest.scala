package ru.agny.xent.core

import org.scalatest.{Matchers, FlatSpec}

class ProductionQueueTest extends FlatSpec with Matchers {

  "ProductionQueue" should "add building item to content" in {
    val item = Outpost("test", Extractable("test", 0, 0, Set.empty), Seq.empty, 0)
    val queue = ProductionQueue(Seq.empty)
    val updated = queue.in(item, 1)
    val expected = Seq((item,1))
    updated.content should be (expected)
  }

  it should "return item if it is done" in {
    val item = Outpost("test", Extractable("test", 0, 0, Set.empty), Seq.empty, 0)
    val queue = ProductionQueue(Seq((item, 1)))
    val (_, res) = queue.out(System.currentTimeMillis())
    val expected = Seq((item,1))
    res should be (expected)
  }

  it should "return Seq.empty if there is no completed items" in {
    val item = Outpost("test", Extractable("test", 0, 0, Set.empty), Seq.empty, 1000)
    val queue = ProductionQueue(Seq((item, 1)))
    val (updated, res) = queue.out(System.currentTimeMillis())
    val expectedQueue = Seq((item,1))
    val expectedRes = Seq.empty
    updated.content should be (expectedQueue)
    res should be (expectedRes)
  }

  it should "remove item from content if it is done" in {
    val item = Outpost("test", Extractable("test", 0, 0, Set.empty), Seq.empty, 0)
    val queue = ProductionQueue(Seq((item, 1)))
    val (updated, _) = queue.out(System.currentTimeMillis())
    val expected = Seq.empty
    updated.content should be (expected)
  }

  it should "reduce content by completed and returned items" in {
    val item = Outpost("test", Extractable("test", 0, 0, Set.empty), Seq.empty, 1000)
    val queue = ProductionQueue(Seq((item, 2)))
    val (updated, res) = queue.out(System.currentTimeMillis() - 1000)
    val expectedQueue = Seq((item,1))
    val expectedRes = Seq((item,1))
    res should be (expectedRes)
    updated.content should be (expectedQueue)
  }

  it should "reduce content by completed and returned items with different entries in ordered fashion" in {
    val item1 = Outpost("test1", Extractable("test1", 0, 0, Set.empty), Seq.empty, 1000)
    val item2 = Outpost("test2", Extractable("test2", 0, 0, Set.empty), Seq.empty, 1000)
    val queue = ProductionQueue(Seq((item1, 2),(item2, 3)))
    val (updated, res) = queue.out(System.currentTimeMillis() - 1000)
    val expectedQueue = Seq((item1,1),(item2, 3))
    val expectedRes = Seq((item1,1))
    res should be (expectedRes)
    updated.content should be (expectedQueue)
  }

  it should "reduce content by completed and returned items with different entries" in {
    val item1 = Outpost("test1", Extractable("test1", 0, 0, Set.empty), Seq.empty, 1000)
    val item2 = Outpost("test2", Extractable("test2", 0, 0, Set.empty), Seq.empty, 1000)
    val queue = ProductionQueue(Seq((item1, 2),(item2, 3)))
    val (updated, res) = queue.out(System.currentTimeMillis() - 3000)
    val expectedQueue = Seq((item2, 2))
    val expectedRes = Seq((item2, 1), (item1,2))
    res should be (expectedRes)
    updated.content should be (expectedQueue)
  }

}
