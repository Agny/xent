package ru.agny.xent.core.utils

import ru.agny.xent.core.{Extractable, WorldCell}
import ru.agny.xent.utils.IdGen
import ru.agny.xent.Layer

import scala.util.Random

object LayerGenerator {

  private val layerSize = 30
  val ids = IdGen()

  def setupLayers(): List[Layer] = (for (i <- 1 to 1) yield Layer(i.toString, i, Seq.empty, generateMap(layerSize, resourceGen(i)), facilityGen(i))).toList

  private def resourceGen(layerLvl: Int): List[ResourceTemplate] = TemplateLoader.loadResource(layerLvl.toString)

  private def facilityGen(layerLvl: Int): List[FacilityTemplate] = TemplateLoader.loadFacility(layerLvl.toString)

  private def generateMap(size: Int, resources: List[ResourceTemplate]): List[WorldCell] = {
    def genByX(x: Int)(y: Int, acc: List[WorldCell]): List[WorldCell] = {
      val mbRes = mbResource(resources)
      if (x < size) genByX(x + 1)(y, WorldCell(x, y, mbRes) :: acc)
      else WorldCell(x, y, mbRes) :: acc
    }

    (0 to size).flatMap(y => genByX(0)(y, List.empty)).toList
  }

  private def mbResource(seed: List[ResourceTemplate]): Option[Extractable] = {
    val threshold = 94
    Random.nextInt(100) match {
      case c if c > threshold => Some(chooseResource(seed))
      case _ => None
    }
  }

  private def chooseResource(from: List[ResourceTemplate]): Extractable = {
    val n = Random.nextInt(from.size)
    val chosen = from(n)
    Extractable(ids.next, chosen.name, randomVolume(chosen.baseVolume), chosen.yieldTime, Set.empty) //TODO since
  }

  private def randomVolume(base: Int): Int = (base * Random.nextDouble()).toInt
}
