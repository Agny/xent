package ru.agny.xent.core.utils

import java.io.File

import ru.agny.xent.core._

import scala.util.Random

object TemplateLoader {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  implicit val formats = DefaultFormats

  def loadProducibles(layer: String): List[Resource] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/composite").toURI)
    val s = resourcesDir.listFiles().toList
    s.map(f => {
      val t = parse(io.Source.fromFile(f).mkString).extract[ProducibleTemplate]
      Producible(t.name, t.cost, t.yieldTime, Set.empty)
    })
  }

  def loadObtainables(layer: String): List[Resource] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/simple").toURI)
    val s = resourcesDir.listFiles().toList
    s.map(f => {
      val t = parse(io.Source.fromFile(f).mkString).extract[SimpleTemplate]
      Obtainable(t.name, t.yieldTime, Set.empty)
    })
  }

  def loadExtractables(layer: String): List[Finite] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/simple/finite").toURI)
    val s = resourcesDir.listFiles().toList
    s.map(f => {
      val t = parse(io.Source.fromFile(f).mkString).extract[FiniteTemplate]
      Extractable(t.name, randomVolume(t.baseVolume), t.yieldTime, Set.empty)
    })
  }

  private def randomVolume(base: Int): Int = (base * Random.nextDouble()).toInt

  def loadFacility(layer: String): List[FacilityTemplate] = {
    val outpostsDir = new File(getClass.getResource(s"/layers/$layer/facility/outpost").toURI)
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    val outpostsTemp = outpostsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[OutpostTemplate])
    val buildingsTemp = buildingsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[BuildingTemplate])
    outpostsTemp ::: buildingsTemp
  }

  def loadBuildings(layer: String): List[BuildingTemplate] = {
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    buildingsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[BuildingTemplate])
  }
}

//TODO since
case class FiniteTemplate(name: String, baseVolume: Int, yieldTime: Long, since: String)
case class SimpleTemplate(name: String, yieldTime: Long, since: String)
case class ProducibleTemplate(name: String, cost: List[ResourceUnit], yieldTime: Long, since: String)
sealed trait FacilityTemplate {
  val name: String
  val cost: List[ResourceUnit]
  val since: String
}
case class OutpostTemplate(name: String, resource: String, cost: List[ResourceUnit], since: String) extends FacilityTemplate
case class BuildingTemplate(name: String, resources: List[String], cost: List[ResourceUnit], since: String) extends FacilityTemplate
