package ru.agny.xent

import ru.agny.xent.item.{DestructibleObject, Resource, Storage}
import ru.agny.xent.realm.Hexagon
import ru.agny.xent.realm.map.{AICity, ResourceDeposit}
import ru.agny.xent.utils.ItemIdGenerator
import ru.agny.xent.war.Defence

import scala.util.Random

/**
 * World layout:
 * 100x100 hexs with center at 0~0 (hence, maxX and maxY == 50)
 * 300 players
 * 900 ai camp
 *
 * ~1 resource per player
 */
object PlacesHelper {
  val MaxProbability = 1000 // mean part goes down to 0.1%
  val MinReachness = 50

  val resourceProbabilities = Resource.values.map {
    case v@Resource.Copper => v -> 10
    case v@Resource.Coal => v -> 5
    case v@Resource.Iron => v -> 5
    case v@Resource.Leather => v -> 10
    case v => v -> 0
  }

  val AICityProbability = 11 // ~ (100x100 - 300) / 900

  def getPlace(pos: Hexagon): Option[DestructibleObject] = {
    getResource(pos).orElse(getAICity(pos))
  }

  def getResource(pos: Hexagon): Option[ResourceDeposit] = {
    resourceProbabilities.find {
      case (r, p) if (p > Random.nextInt(MaxProbability)) => true
      case _ => false
    }.map { case (r, _) =>
      ResourceDeposit(
        ItemIdGenerator.next,
        r,
        MinReachness + Random.nextInt(MinReachness),
        pos
      )
    }
  }

  def getAICity(pos: Hexagon): Option[AICity] = {
    if (AICityProbability > Random.nextInt(MaxProbability)) {
      Some(AICity(
        ItemIdGenerator.next,
        Defence.Empty,
        Storage.Empty,
        pos
      ))
    }
    else None
  }
}
