package ru.agny.xent

import scala.util.Random

object LayerGenerator {

  private val layerSize = 30

  def setupLayers(): List[Layer] = (for (i <- 1 to 1) yield Layer(i.toString, i, Seq.empty, generateMap(layerSize, resourceGen(i)), facilityGen(i))).toList

  private def resourceGen(layerLvl: Int): List[Extractable] = TemplateLoader.loadResource(layerLvl.toString)

  private def facilityGen(layerLvl: Int): List[FacilityTemplate] = TemplateLoader.loadFacility(layerLvl.toString)

  private def generateMap(size: Int, resources: List[Extractable]): List[WorldCell] = {
    def genByX(x: Int)(y: Int, acc: List[WorldCell]): List[WorldCell] = {
      if (x < size) genByX(x + 1)(y, WorldCell(x, y, mbResource(resources)) :: acc)
      else WorldCell(x, y, mbResource(resources)) :: acc
    }

    (0 to size).flatMap(y => genByX(0)(y, List.empty)).toList
  }

  private def mbResource(seed: List[Extractable]): Option[Extractable] = {
    val threshold = 94
    Random.nextInt(100) match {
      case c if c > threshold => Some(chooseResource(seed))
      case _ => None
    }
  }

  private def chooseResource(from: List[Extractable]): Extractable = {
    val n = Random.nextInt(from.size)
    val chosen = from(n)
    chosen.copy(volume = randomVolume(chosen.volume))
  }

  private def randomVolume(base: Int): Int = (base * Random.nextDouble()).toInt
}
