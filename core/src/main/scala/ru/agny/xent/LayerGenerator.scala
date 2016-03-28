package ru.agny.xent

import scala.util.Random

object LayerGenerator {

  def setupLayers(): List[Layer] = (for (i <- 1 to 7) yield Layer(i.toString, i, resourceGen(i), facilityGen(i))).toList

  def resourceGen(layerLvl: Int): List[Extractable] = TemplateLoader.loadResource(layerLvl.toString)

  def facilityGen(layerLvl: Int): List[FacilityTemplate] = TemplateLoader.loadFacility(layerLvl.toString)

  def mbResource(seed: List[Extractable]): Option[Extractable] = {
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
