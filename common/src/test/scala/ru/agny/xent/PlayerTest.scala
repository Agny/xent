package ru.agny.xent

import org.scalactic.source.Position
import org.scalatest.{Args, Status}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import scala.language.implicitConversions

class PlayerTest extends AnyFlatSpec  {

  //still "no implicit found"
  given Position = Position("PlayerTest", "ru.agny.xent", 42)

  "Player" should "not be hostile to himself" in {
    val p = Player(1)
    p.isFriendly(p) should be(true) 
  }
  
  it should "be hostile to others" in {
    val p1 = Player(1)
    val p2 = Player(2)
    p1.isFriendly(p2) should be(false)
  }

  "AIEnemy" should "be hostile to all" in {
    val ai = Player.AIEnemy
    val p = Player(1)
    ai.isFriendly(ai) should be(false)
    ai.isFriendly(p) should be(false)
  }

  override def run(testName: Option[String], args: Args): Status = super.run(testName, args)

  protected override def runTest(testName: String, args: Args): Status = super.runTest(testName, args)
}
