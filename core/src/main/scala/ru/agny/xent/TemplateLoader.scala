package ru.agny.xent

import java.io.File

import ru.agny.xent.utils.IdGen

object TemplateLoader {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  implicit val formats = DefaultFormats
  val idGen = IdGen()

  //load resource templates in path
  def loadResource(layer: String): List[Extractable] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource").toURI)
    val s = resourcesDir.listFiles().toList
    s.map(f => parse(io.Source.fromFile(f).mkString).extract[ResourceTemplate]).map(r => fromTemplate(r))
  }

  def loadFacility(layer: String): List[FacilityTemplate] = {
    val outpostsDir = new File(getClass.getResource(s"/layers/$layer/facility/outpost").toURI)
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    val outpostsTemp = outpostsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[OutpostTemplate])
    val buildingsTemp = buildingsDir.listFiles().toList.map(f => parse(io.Source.fromFile(f).mkString).extract[BuildingTemplate])
    outpostsTemp ::: buildingsTemp
  }

  private def fromTemplate(t: ResourceTemplate): Extractable = {
    Extractable(idGen.next, t.name, t.baseVolume, since(t.since))
  }

  //todo
  private def since(paramName: String): Set[Prereq] = {
    Set.empty
  }
}

case class ResourceTemplate(name: String, baseVolume: Int, since: String)
sealed trait FacilityTemplate {
  val name: String
  val recipe: RecipeTemplate
  val since: String
}
case class OutpostTemplate(name: String, resource: String, recipe: RecipeTemplate, since: String) extends FacilityTemplate
case class BuildingTemplate(name: String, resources: List[String], recipe: RecipeTemplate, since: String) extends FacilityTemplate
case class RecipeTemplate(cost: List[ResourceUnitTemplate])
case class ResourceUnitTemplate(value: Int, res: String)
