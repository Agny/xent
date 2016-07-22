package ru.agny.xent.core.utils

import ru.agny.xent.core._
import ru.agny.xent.utils.IdGen
import ru.agny.xent.Layer

import scala.util.Random

object LayerGenerator {

  private val layerSize = 30
  val ids = IdGen()

  def setupLayers(): List[Layer] = (for (i <- 1 to 1) yield Layer(i.toString, i, Seq.empty, generateWorldMap(layerSize, resourceGen(i)), facilityGen(i))).toList

  private def resourceGen(layerLvl: Int): List[Finite] = TemplateLoader.loadExtractables(layerLvl.toString)

  private def facilityGen(layerLvl: Int): List[FacilityTemplate] = TemplateLoader.loadFacility(layerLvl.toString)

  private def generateWorldMap(size: Int, resources: List[Finite]): CellsMap[WorldCell] = {
    def genByY(y: Int)(x: Int, acc: List[WorldCell]): List[WorldCell] = {
//      val mbRes = mbResource(resources)
      val mbRes =
        if (x == 1 && y == 2) Some(Extractable("Copper", 51, 200, Set.empty))            //test purposes
        else mbResource(resources)
      if (y < size) genByY(y + 1)(y, WorldCell(x, y, mbRes) :: acc)
      else WorldCell(x, y, mbRes) :: acc
    }

    CellsMap((0 to size).map(x => genByY(0)(x, List.empty).reverse.toVector).toVector)
  }

  private def mbResource(seed: List[Finite]): Option[Finite] = {
    val threshold = 94
    Random.nextInt(100) match {
      case c if c > threshold => Some(chooseResource(seed))
      case _ => None
    }
  }

  private def chooseResource(from: List[Finite]): Finite = {
    val n = Random.nextInt(from.size)
    from(n)
  }
}
