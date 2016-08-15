package ru.agny.xent.core.utils

import java.io.File

import ru.agny.xent.ResourceUnit
import ru.agny.xent.core._

import scala.util.Random

object TemplateLoader {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  private implicit val formats = DefaultFormats

  def loadProducibles(layer: String): List[Resource] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/composite").toURI)
    val s = resourcesDir.listFiles().toList.filter(_.isFile)
    s.map(f => {
      val t = parse(io.Source.fromFile(f).mkString).extract[ProducibleTemplate]
      Producible(t.name, t.cost, t.yieldTime, Set.empty)
    })
  }

  def loadObtainables(layer: String): List[Resource] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/simple").toURI)
    val s = resourcesDir.listFiles().toList.filter(_.isFile)
    s.map(f => {
      val t = parse(io.Source.fromFile(f).mkString).extract[SimpleTemplate]
      Obtainable(t.name, t.yieldTime, Set.empty)
    })
  }

  def loadExtractables(layer: String): List[Extractable] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/simple/finite").toURI)
    val s = resourcesDir.listFiles().toList.filter(_.isFile)
    s.map(f => {
      val t = parse(io.Source.fromFile(f).mkString).extract[FiniteTemplate]
      Extractable(t.name, randomVolume(t.baseVolume), t.yieldTime, Set.empty)
    })
  }

  private def randomVolume(base: Int): Int = (base * Random.nextDouble()).toInt

  def loadOutposts(layer: String): List[OutpostTemplate] = {
    val producibles = loadProducibles(layer)
    val obtainables = loadObtainables(layer)
    val outpostsDir = new File(getClass.getResource(s"/layers/$layer/facility/outpost").toURI)
    outpostsDir.listFiles().toList.map(f => {
      val json = parse(io.Source.fromFile(f).mkString).extract[OutpostTemplateJson]
      val pres = json.producible.flatMap(res => producibles.find(x => x.name == res).map(x => x))
      val ores = json.obtainable.flatMap(res => obtainables.find(x => x.name == res).map(x => x))
      OutpostTemplate(json.name, json.extractable,  pres ::: ores, json.cost, json.since)
    })
  }

  def loadBuildings(layer: String): List[BuildingTemplate] = {
    val producibles = loadProducibles(layer)
    val obtainables = loadObtainables(layer)
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    buildingsDir.listFiles().toList.map(f => {
      val json = parse(io.Source.fromFile(f).mkString).extract[BuildingTemplateJson]
      val pres = json.producible.flatMap(res => producibles.find(x => x.name == res).map(x => x))
      val ores = json.obtainable.flatMap(res => obtainables.find(x => x.name == res).map(x => x))
      BuildingTemplate(json.name, pres ::: ores, json.cost, json.since)
    })
  }
}

//TODO since
case class FiniteTemplate(name: String, baseVolume: Int, yieldTime: Long, since: String)
case class SimpleTemplate(name: String, yieldTime: Long, since: String)
case class ProducibleTemplate(name: String, cost: List[ResourceUnit], yieldTime: Long, since: String)
sealed trait FacilityTemplate extends Cost{
  val name: String
  val resources: List[Resource]
  val cost: List[ResourceUnit]
  val since: String
}
case class OutpostTemplate(name: String, extractable: String, resources: List[Resource], cost: List[ResourceUnit], since: String) extends FacilityTemplate
case class BuildingTemplate(name: String, resources: List[Resource], cost: List[ResourceUnit], since: String) extends FacilityTemplate

case class OutpostTemplateJson(name: String, extractable: String, obtainable: List[String], producible: List[String], cost: List[ResourceUnit], since: String)
case class BuildingTemplateJson(name: String, obtainable: List[String], producible: List[String], cost: List[ResourceUnit], since: String)
