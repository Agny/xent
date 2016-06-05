package ru.agny.xent

import java.io.File

import ru.agny.xent.utils.IdGen

object TemplateLoader {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  implicit val formats = DefaultFormats

  //load resource templates in path
  def loadResource(layer: String): List[ResourceTemplate] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource").toURI)
    val s = resourcesDir.listFiles().toList
    s.map(f => parse(io.Source.fromFile(f).mkString).extract[ResourceTemplate])
  }

  def loadFacility(layer: String): List[FacilityTemplate] = {
    val outpostsDir = new File(getClass.getResource(s"/layers/$layer/facility/outpost").toURI)
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    val outpostsTemp = outpostsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[OutpostTemplate])
    val buildingsTemp = buildingsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[BuildingTemplate])
    outpostsTemp ::: buildingsTemp
  }
}

case class ResourceTemplate(name: String, baseVolume: Int, yieldTime:Long, since: String)
sealed trait FacilityTemplate {
  val name: String
  val cost: List[ResourceUnit]
  val since: String
}
case class RecipeTemplate(producible:String, cost:List[ResourceUnit])
case class OutpostTemplate(name: String, resource: String, cost: List[ResourceUnit], since: String) extends FacilityTemplate
case class BuildingTemplate(name: String, resources: List[String], cost: List[ResourceUnit], since: String) extends FacilityTemplate
