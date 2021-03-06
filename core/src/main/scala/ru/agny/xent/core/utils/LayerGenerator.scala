package ru.agny.xent.core.utils

import ru.agny.xent.battle.Military
import ru.agny.xent.core.{Layer, _}
import ru.agny.xent.core.inventory.Extractable

import scala.util.Random

object LayerGenerator {

  private val layerSize = 30

  def setupLayers(): Vector[Layer] = (for (i <- 1 to 1) yield {
    TemplateProvider.add(i.toString, facilityGen(i))
    Layer(i.toString, i, Vector.empty, Military.empty, generateWorldMap(layerSize, resourceGen(i)))
  }).toVector

  private def resourceGen(layerLvl: Int): Vector[Extractable] = TemplateLoader.loadExtractables(layerLvl.toString)

  private def facilityGen(layerLvl: Int): Vector[FacilityTemplate] = TemplateLoader.loadBuildings(layerLvl.toString) ++ TemplateLoader.loadOutposts(layerLvl.toString)

  def generateWorldMap(size: Int, resources: Vector[Extractable]): CellsMap = {
    def genByY(y: Int)(x: Int, acc: Vector[Cell]): Vector[Cell] = {
      //      val mbRes = mbResource(resources)
      val mbRes =
        if (x == 1 && y == 2) Some(Extractable(1, "Copper", 51, 3000, 14, Set.empty)) //test purposes
        else mbResource(resources)

      val cell = mbRes match {
        case Some(v) => ResourceCell(Coordinate(x, y), v)
        case None => Cell(x, y)
      }

      if (y < size) genByY(y + 1)(x, cell +: acc)
      else cell +: acc
    }

    CellsMap((0 to size).toVector.map(x => genByY(0)(x, Vector.empty).reverse))
  }

  //TODO actual implementation
  def newCityCoordinate(layer: Layer): Option[Coordinate] = {
    layer.map.filter {
      case _: EmptyCell => true
      case _ => false
    } match {
      case h +: t => Some(h.c)
      case _ => None
    }
  }

  private def mbResource(seed: Vector[Extractable]): Option[Extractable] = {
    val threshold = 94
    Random.nextInt(100) match {
      case c if c > threshold && seed.nonEmpty => Some(chooseResource(seed))
      case _ => None
    }
  }

  private def chooseResource(from: Vector[Extractable]): Extractable = {
    val n = Random.nextInt(from.size)
    val res = from(n)
    res.copy(volume = randomVolume(res.volume))
  }

  // not less than 20% of base value
  private def randomVolume(base: Int): Int = {
    var k = Random.nextDouble()
    if (k < 0.2) k = 0.2
    (base * k).toInt
  }
}
