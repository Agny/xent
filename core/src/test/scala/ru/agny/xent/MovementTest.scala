package ru.agny.xent

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.realm.{Hexagon, Movement}

import scala.language.implicitConversions

class MovementTest extends AnyFlatSpec {

  "Movement" should "go through path" in {
    val p = Hexagon(0, 0).path(Hexagon(2, 2)) 
    val m = Movement(p)

    m.isDestinationReached() should be(false)
    
    m.tick(Velocity.Max, 3600)
    m.isDestinationReached() should be(false) //first hex

    m.tick(0,0)
    m.isDestinationReached() should be(false) //second hex

    m.tick(0,0)
    m.isDestinationReached() should be(false) //third hex

    m.tick(0,0)
    m.isDestinationReached() should be(false) //last hex
    
    m.tick(0,0)
    m.isDestinationReached() should be(true) //last hex center
  }

}
