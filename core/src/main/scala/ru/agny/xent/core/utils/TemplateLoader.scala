package ru.agny.xent.core.utils

import java.io.File
import io.Source._
import ru.agny.xent.core._
import Item.ItemId

import ru.agny.xent.core._

object TemplateLoader {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  private implicit val formats = DefaultFormats

  def loadProducibles(layer: String): Vector[Producible] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/item/producible").toURI)
    val s = resourcesDir.listFiles().toVector.filter(_.isFile)
    s.map(f => {
      val t = parse(fromFile(f).mkString).extract[ProducibleTemplate]
      IdHolder.add(t.name, t.id)
      Producible(t.id, t.name, ProductionSchema(t.yieldTime, Cost(t.cost), Set.empty))
    })
  }

  def loadObtainables(layer: String): Vector[Obtainable] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/item/obtainable").toURI)
    val s = resourcesDir.listFiles().toVector.filter(_.isFile)
    s.map(f => {
      val t = parse(fromFile(f).mkString).extract[SimpleTemplate]
      IdHolder.add(t.name, t.id)
      Obtainable(t.id, t.name, t.yieldTime, Set.empty)
    })
  }

  def loadExtractables(layer: String): Vector[Extractable] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$layer/item/extractable").toURI)
    val s = resourcesDir.listFiles().toVector.filter(_.isFile)
    s.map(f => {
      val t = parse(fromFile(f).mkString).extract[FiniteTemplate]
      IdHolder.add(t.name, t.id)
      Extractable(t.id, t.name, t.baseVolume, t.yieldTime, Set.empty)
    })
  }

  def loadOutposts(layer: String): Vector[OutpostTemplate] = {
    val producibles = loadProducibles(layer)
    val obtainables = loadObtainables(layer)
    val outpostsDir = new File(getClass.getResource(s"/layers/$layer/facility/outpost").toURI)
    outpostsDir.listFiles().toVector.map(f => {
      val t = parse(fromFile(f).mkString).extract[OutpostTemplateJson]
      val ores = t.obtainable.flatMap(res => obtainables.find(x => x.name == res).map(x => x))
      IdHolder.add(t.name, t.id)
      OutpostTemplate(t.id, t.name, t.extractable, ores, Vector.empty, Cost(t.cost), t.buildTime, t.since)
    })
  }

  def loadBuildings(layer: String): Vector[BuildingTemplate] = {
    val producibles = loadProducibles(layer)
    val obtainables = loadObtainables(layer)
    val buildingsDir = new File(getClass.getResource(s"/layers/$layer/facility/building").toURI)
    buildingsDir.listFiles().toVector.map(f => {
      val t = parse(fromFile(f).mkString).extract[BuildingTemplateJson]
      val pres = t.producible.flatMap(res => producibles.find(x => x.name == res).map(x => x))
      val ores = t.obtainable.flatMap(res => obtainables.find(x => x.name == res).map(x => x))
      IdHolder.add(t.name, t.id)
      BuildingTemplate(t.id, t.name, pres, ores, Cost(t.cost), t.buildTime, t.shape, t.since)
    })
  }
}

//TODO since
case class FiniteTemplate(id: ItemId, name: String, baseVolume: Int, yieldTime: Long, since: String)
case class SimpleTemplate(id: ItemId, name: String, yieldTime: Long, since: String)
case class ProducibleTemplate(id: ItemId, name: String, cost: Vector[ItemStack], yieldTime: Long, since: String)
sealed trait FacilityTemplate {
  val id: ItemId
  val name: String
  val producibles: Vector[Producible]
  val obtainables: Vector[Obtainable]
  val cost: Cost
  val buildTime: Long
  val since: String
}
case class OutpostTemplate(id: ItemId, name: String, extractable: String, obtainables: Vector[Obtainable], producibles: Vector[Producible], cost: Cost, buildTime: Long, since: String) extends FacilityTemplate
case class BuildingTemplate(id: ItemId, name: String, producibles: Vector[Producible], obtainables: Vector[Obtainable], cost: Cost, buildTime: Long, shape: FourShape, since: String) extends FacilityTemplate

case class OutpostTemplateJson(id: ItemId, name: String, extractable: String, obtainable: Vector[String], cost: Vector[ItemStack], buildTime: Long, since: String)
case class BuildingTemplateJson(id: ItemId, name: String, obtainable: Vector[String], producible: Vector[String], cost: Vector[ItemStack], buildTime: Long, shape: FourShape, since: String)
