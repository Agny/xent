package ru.agny.xent

import ru.agny.xent.item.DestructibleObject
import ru.agny.xent.realm.{GameMap, Realm}
import ru.agny.xent.realm.Hexagon._

import java.util.UUID

object WorldBuilder {
  given PlayerService = Player.defaultPS

  val MaxX, MaxY = 50

  def main(args: Array[String]): Unit = {
    println(getPlaces(-5,5,-5,5))
  }

  private def getRealm(): Realm = {
    Realm(UUID.randomUUID(), getMap())
  }

  private def getMap(): GameMap = {
    GameMap(MaxX, MaxY, getPlaces(-MaxX, MaxX, -MaxY, MaxY), ???)
  }

  private def getPlaces(fromX: Int, toX: Int, fromY: Int, toY: Int): Seq[DestructibleObject] = {
    fromX to toX flatMap { x =>
      fromY to toY flatMap { y =>
        PlacesHelper.getPlace(x ~ y)
      }
    }
  }
}
