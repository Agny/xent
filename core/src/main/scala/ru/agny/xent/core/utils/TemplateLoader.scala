package ru.agny.xent.core.utils

import java.io.File
import io.Source._
import ru.agny.xent.core.Item.ItemId

import ru.agny.xent.core._

object TemplateLoader {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  private implicit val formats = DefaultFormats

  def loadProducibles(layer: String): Seq[Resource] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/composite").toURI)
    val s = resourcesDir.listFiles().toSeq.filter(_.isFile)
    s.map(f => {
      val t = parse(fromFile(f).mkString).extract[ProducibleTemplate]
      IdHolder.add(t.name, t.id)
      Producible(t.id, t.name, t.cost, t.yieldTime, Set.empty)
    })
  }

  def loadObtainables(layer: String): Seq[Resource] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/simple").toURI)
    val s = resourcesDir.listFiles().toSeq.filter(_.isFile)
    s.map(f => {
      val t = parse(fromFile(f).mkString).extract[SimpleTemplate]
      IdHolder.add(t.name, t.id)
      Obtainable(t.id, t.name, t.yieldTime, Set.empty)
    })
  }

  def loadExtractables(layer: String): Seq[Extractable] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/resource/simple/finite").toURI)
    val s = resourcesDir.listFiles().toSeq.filter(_.isFile)
    s.map(f => {
      val t = parse(fromFile(f).mkString).extract[FiniteTemplate]
      IdHolder.add(t.name, t.id)
      Extractable(t.id, t.name, t.baseVolume, t.yieldTime, Set.empty)
    })
  }

  def loadOutposts(layer: String): Seq[OutpostTemplate] = {
    val producibles = loadProducibles(layer)
    val obtainables = loadObtainables(layer)
    val outpostsDir = new File(getClass.getResource(s"/layers/$layer/facility/outpost").toURI)
    outpostsDir.listFiles().toSeq.map(f => {
      val t = parse(fromFile(f).mkString).extract[OutpostTemplateJson]
      val pres = t.producible.flatMap(res => producibles.find(x => x.name == res).map(x => x))
      val ores = t.obtainable.flatMap(res => obtainables.find(x => x.name == res).map(x => x))
      IdHolder.add(t.name, t.id)
      OutpostTemplate(t.id, t.name, t.extractable, pres ++ ores, t.cost, t.buildTime, t.since)
    })
  }

  def loadBuildings(layer: String): Seq[BuildingTemplate] = {
    val producibles = loadProducibles(layer)
    val obtainables = loadObtainables(layer)
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    buildingsDir.listFiles().toSeq.map(f => {
      val t = parse(fromFile(f).mkString).extract[BuildingTemplateJson]
      val pres = t.producible.flatMap(res => producibles.find(x => x.name == res).map(x => x))
      val ores = t.obtainable.flatMap(res => obtainables.find(x => x.name == res).map(x => x))
      IdHolder.add(t.name, t.id)
      BuildingTemplate(t.id, t.name, pres ++ ores, t.cost, t.buildTime, t.shape, t.since)
    })
  }
}

//TODO since
case class FiniteTemplate(id: ItemId, name: String, baseVolume: Int, yieldTime: Long, since: String)
case class SimpleTemplate(id: ItemId, name: String, yieldTime: Long, since: String)
case class ProducibleTemplate(id: ItemId, name: String, cost: Seq[ResourceUnit], yieldTime: Long, since: String)
sealed trait FacilityTemplate extends Cost {
  val id: ItemId
  val name: String
  val resources: Seq[Resource]
  val cost: Seq[ResourceUnit]
  val buildTime: Long
  val since: String
}
case class OutpostTemplate(id:ItemId, name: String, extractable: String, resources: Seq[Resource], cost: Seq[ResourceUnit], buildTime: Long, since: String) extends FacilityTemplate
case class BuildingTemplate(id:ItemId, name: String, resources: Seq[Resource], cost: Seq[ResourceUnit], buildTime: Long, shape: FourShape, since: String) extends FacilityTemplate

case class OutpostTemplateJson(id:ItemId, name: String, extractable: String, obtainable: Seq[String], producible: Seq[String], cost: Seq[ResourceUnit], buildTime: Long, since: String)
case class BuildingTemplateJson(id:ItemId, name: String, obtainable: Seq[String], producible: Seq[String], cost: Seq[ResourceUnit], buildTime: Long, shape: FourShape, since: String)
