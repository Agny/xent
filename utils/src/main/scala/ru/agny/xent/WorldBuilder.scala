package ru.agny.xent

import com.typesafe.config.ConfigFactory
import ru.agny.xent.item.DestructibleObject
import ru.agny.xent.realm.{GameMap, Realm}
import ru.agny.xent.realm.Hexagon._

import java.util.UUID

object WorldBuilder {

  val conf = ConfigFactory.load()
  val xMax = conf.getInt("world.xMax")
  val yMax = conf.getInt("world.yMax")

  def main(args: Array[String]): Unit = {
    println(getRealm())
  }

  private def getRealm(): Realm = {
    val s = Realm(UUID.randomUUID(), Player.defaultPS, getMap(), 0L)
    s.handle(Seq(Message.Event(1L, 0L, Action.Noop)))
    s
  }

  private def getMap(): GameMap = {
    GameMap(xMax, yMax, getPlaces(xMax, yMax), Seq.empty)
  }

  private def getPlaces(x: Int, y: Int): Seq[DestructibleObject] = {
    -x to x flatMap { x =>
      -y to y flatMap { y =>
        PlacesHelper.getPlace(x ~ y)
      }
    }
  }
}
