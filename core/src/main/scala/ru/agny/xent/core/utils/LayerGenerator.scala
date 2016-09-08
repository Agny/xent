package ru.agny.xent.core.utils

import ru.agny.xent.core._
import ru.agny.xent.Layer

import scala.util.Random

object LayerGenerator {

  private val layerSize = 30

  def setupLayers(): Seq[Layer] = for (i <- 1 to 1) yield Layer(i.toString, i, Seq.empty, generateWorldMap(layerSize, resourceGen(i)), facilityGen(i))

  private def resourceGen(layerLvl: Int): Seq[Extractable] = TemplateLoader.loadExtractables(layerLvl.toString)

  private def facilityGen(layerLvl: Int): Seq[FacilityTemplate] = TemplateLoader.loadBuildings(layerLvl.toString) ++ TemplateLoader.loadOutposts(layerLvl.toString)

  private def generateWorldMap(size: Int, resources: Seq[Extractable]): CellsMap[WorldCell] = {
    def genByY(y: Int)(x: Int, acc: Seq[WorldCell]): Seq[WorldCell] = {
//      val mbRes = mbResource(resources)
      val mbRes =
        if (x == 1 && y == 2) Some(Extractable("Copper", 51, 3000, Set.empty))            //test purposes
        else mbResource(resources)
      if (y < size) genByY(y + 1)(x, WorldCell(x, y, mbRes) +: acc)
      else WorldCell(x, y, mbRes) +: acc
    }

    CellsMap((0 to size).map(x => genByY(0)(x, Seq.empty).reverse))
  }

  private def mbResource(seed: Seq[Extractable]): Option[Extractable] = {
    val threshold = 94
    Random.nextInt(100) match {
      case c if c > threshold => Some(chooseResource(seed))
      case _ => None
    }
  }

  private def chooseResource(from: Seq[Extractable]): Extractable = {
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
