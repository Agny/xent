package ru.agny.xent.core.utils

import java.io.File

import ru.agny.xent.core.ResourceUnit

object TemplateLoader {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  implicit val formats = DefaultFormats

  //load resource templates in path
  def loadExtractables(layer: String): List[ExtractableTemplate] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/extract").toURI)
    val s = resourcesDir.listFiles().toList
    s.map(f => parse(io.Source.fromFile(f).mkString).extract[ExtractableTemplate])
  }

  def loadProducibles(layer: String): List[ProducibleTemplate] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/produce").toURI)
    val s = resourcesDir.listFiles().toList
    s.map(f => parse(io.Source.fromFile(f).mkString).extract[ProducibleTemplate])
  }

  def loadFacility(layer: String): List[FacilityTemplate] = {
    val outpostsDir = new File(getClass.getResource(s"/layers/$layer/facility/outpost").toURI)
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    val outpostsTemp = outpostsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[OutpostTemplate])
    val buildingsTemp = buildingsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[BuildingTemplate])
    outpostsTemp ::: buildingsTemp
  }

  def loadBuildings(layer:String):List[BuildingTemplate] = {
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    buildingsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[BuildingTemplate])
  }
}

//TODO since
case class ExtractableTemplate(name: String, baseVolume: Int, yieldTime: Long, since: String)
case class ProducibleTemplate(name: String, cost: List[ResourceUnit], yieldTime: Long, since: String)
sealed trait FacilityTemplate {
  val name: String
  val cost: List[ResourceUnit]
  val since: String
}
case class OutpostTemplate(name: String, resource: String, cost: List[ResourceUnit], since: String) extends FacilityTemplate
case class BuildingTemplate(name: String, resources: List[String], cost: List[ResourceUnit], since: String) extends FacilityTemplate
