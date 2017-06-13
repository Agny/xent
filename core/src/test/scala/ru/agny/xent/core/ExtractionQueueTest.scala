package ru.agny.xent.core

import org.scalatest.{Matchers, FlatSpec}

class ExtractionQueueTest extends FlatSpec with Matchers {

  val extrId = 1
  val otherId = 2

  "ExtractionQueue" should "not change by item adding" in {
    val item = Extractable(extrId, "test", 0, 0, Set.empty)
    val other = Extractable(otherId, "test2", 0, 0, Set.empty)
    val expected = ExtractionQueue(item)
    val result = expected.in(other, 4)
    result should be theSameInstanceAs expected
  }

  it should "return correct amount of produced items" in {
    val item = Extractable(extrId, "test", 6, 1000, Set.empty)
    val queue = ExtractionQueue(item)
    val (_, result) = queue.out(5000)
    result should be(Vector((item, 5)))
  }

  it should "save progress" in {
    val item = Extractable(extrId, "test", 6, 1000, Set.empty)
    val queue = ExtractionQueue(item)
    val (q, result) = queue.out(3300)
    result should be(Vector((item, 3)))
    q.progress should be(300)
  }

  it should "mutate source of extractable" in {
    val item = Extractable(extrId, "test", 6, 1000, Set.empty)
    val queue = ExtractionQueue(item)
    val (q, result) = queue.out(5000)
    result should be(Vector((item, 5)))
    q.content.volume should be(1)
    item should be theSameInstanceAs q.content
  }

}
