package ru.agny.xent.core

import org.scalatest.{Matchers, FlatSpec}

class ProductionQueueTest extends FlatSpec with Matchers {

  val extrId1 = 1
  val extrId2 = 2

  "ProductionQueue" should "add building item to content" in {
    val item = Outpost("test", Extractable(extrId1, "test", 0, 0, Set.empty), Vector.empty, 0)
    val queue = ProductionQueue.empty
    val updated = queue.in(item, 1)
    val expected = Vector((item,1))
    updated.content should be (expected)
  }

  it should "return item if it is done" in {
    val item = Outpost("test", Extractable(extrId1, "test", 0, 0, Set.empty), Vector.empty, 0)
    val queue = ProductionQueue(Vector((item, 1)))
    val (_, res) = queue.out(0)
    val expected = Vector((item,1))
    res should be (expected)
  }

  it should "return Vector.empty if there is no completed items" in {
    val item = Outpost("test", Extractable(extrId1, "test", 0, 0, Set.empty), Vector.empty, 1000)
    val queue = ProductionQueue(Vector((item, 1)))
    val (updated, res) = queue.out(0)
    val expectedQueue = Vector((item,1))
    val expectedRes = Vector.empty
    updated.content should be (expectedQueue)
    res should be (expectedRes)
  }

  it should "remove item from content if it is done" in {
    val item = Outpost("test", Extractable(extrId1, "test", 0, 0, Set.empty), Vector.empty, 0)
    val queue = ProductionQueue(Vector((item, 1)))
    val (updated, _) = queue.out(0)
    val expected = Vector.empty
    updated.content should be (expected)
  }

  it should "reduce content by completed and returned items" in {
    val item = Outpost("test", Extractable(extrId1, "test", 0, 0, Set.empty), Vector.empty, 1000)
    val queue = ProductionQueue(Vector((item, 2)))
    val (updated, res) = queue.out(1000)
    val expectedQueue = Vector((item,1))
    val expectedRes = Vector((item,1))
    res should be (expectedRes)
    updated.content should be (expectedQueue)
  }

  it should "reduce content by completed and returned items with different entries in ordered fashion" in {
    val item1 = Outpost("test1", Extractable(extrId1, "test1", 0, 0, Set.empty), Vector.empty, 1000)
    val item2 = Outpost("test2", Extractable(extrId2, "test2", 0, 0, Set.empty), Vector.empty, 1000)
    val queue = ProductionQueue(Vector((item1, 2),(item2, 3)))
    val (updated, res) = queue.out(1000)
    val expectedQueue = Vector((item1,1),(item2, 3))
    val expectedRes = Vector((item1,1))
    res should be (expectedRes)
    updated.content should be (expectedQueue)
  }

  it should "reduce content by completed and returned items with different entries" in {
    val item1 = Outpost("test1", Extractable(extrId1, "test1", 0, 0, Set.empty), Vector.empty, 1000)
    val item2 = Outpost("test2", Extractable(extrId2, "test2", 0, 0, Set.empty), Vector.empty, 1000)
    val queue = ProductionQueue(Vector((item1, 2),(item2, 3)))
    val (updated, res) = queue.out(3000)
    val expectedQueue = Vector((item2, 2))
    val expectedRes = Vector((item2, 1), (item1,2))
    res should be (expectedRes)
    updated.content should be (expectedQueue)
  }

  it should "reduce time if item is not completed" in {
    val item1 = Outpost("test1", Extractable(extrId1, "test1", 0, 0, Set.empty), Vector.empty, 1000)
    val queue = ProductionQueue(Vector((item1, 1)))
    val (updated1, _) = queue.out(300)
    val (updated2, _) = updated1.out(300)
    val (updated3, res) = updated2.out(400)
    val expectedQueue = Vector.empty
    val expectedRes = Vector((item1,1))
    updated3.content should be (expectedQueue)
    res should be (expectedRes)
  }

  it should "not accumulate progress when content is empty" in {
    val queue = ProductionQueue.empty
    val (updated, _) = queue.out(1000)
    val expectedQueue = Vector.empty
    val expectedTime = 0
    updated.content should be (expectedQueue)
    updated.progress should be (expectedTime)
  }

  it should "not accumulate progress after content becomes empty" in {
    val item1 = Outpost("test1", Extractable(extrId1, "test1", 0, 0, Set.empty), Vector.empty, 1000)
    val queue = ProductionQueue(Vector((item1,1)))
    val (updated, _) = queue.out(1500)
    val expectedQueue = Vector.empty
    val expectedTime = 0
    updated.content should be (expectedQueue)
    updated.progress should be (expectedTime)
  }

}
