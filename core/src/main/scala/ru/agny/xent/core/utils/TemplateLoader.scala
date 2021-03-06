package ru.agny.xent.core.utils

import java.io.File

import scala.io.Source._
import ru.agny.xent.core.city.ShapeProvider
import ru.agny.xent.core.inventory.Item.{ItemId, ItemWeight}
import ru.agny.xent.core.inventory._

object TemplateLoader {

  import io.circe.generic.auto._
  import io.circe.parser._

  def loadProducibles(layer: String): Vector[Producible] = {
    val resourcesDir = new File(getClass.getClassLoader.getResource(s"./layers/$layer/item/producible").getPath)
    val s = resourcesDir.listFiles().toVector.filter(_.isFile)
    s.map(f => {
      val t = decode[ProducibleTemplate](fromFile(f).mkString).right.get
      Producible(t.id, t.name, ProductionSchema(t.yieldTime, Cost(t.cost), t.weight, Set.empty))
    })
  }

  def loadObtainables(layer: String): Vector[Obtainable] = {
    val resourcesDir = new File(getClass.getClassLoader.getResource(s"./layers/$layer/item/obtainable").getPath)
    val s = resourcesDir.listFiles().toVector.filter(_.isFile)
    s.map(f => {
      val t = decode[ObtainableTemplate](fromFile(f).mkString).right.get
      Obtainable(t.id, t.name, t.yieldTime, t.weight, Set.empty)
    })
  }

  def loadExtractables(layer: String): Vector[Extractable] = {
    val resourcesDir = new File(getClass.getClassLoader.getResource(s"./layers/$layer/item/extractable").getPath)
    val s = resourcesDir.listFiles().toVector.filter(_.isFile)
    s.map(f => {
      val t = decode[ExtractableTemplate](fromFile(f).mkString).right.get
      Extractable(t.id, t.name, t.baseVolume, t.yieldTime, t.weight, Set.empty)
    })
  }

  def loadOutposts(layer: String): Vector[OutpostTemplate] = {
    val obtainables = loadObtainables(layer)
    val outpostsDir = new File(getClass.getClassLoader.getResource(s"./layers/$layer/facility/outpost").getPath)
    outpostsDir.listFiles().toVector.map(f => {
      val t = decode[OutpostTemplateJson](fromFile(f).mkString).right.get
      val ores = t.obtainable.flatMap(res => obtainables.find(x => x.name == res).map(x => x))
      OutpostTemplate(t.name, t.extractable, ores, Vector.empty, Cost(t.cost), t.buildTime, t.since)
    })
  }

  def loadBuildings(layer: String): Vector[BuildingTemplate] = {
    val producibles = loadProducibles(layer)
    val obtainables = loadObtainables(layer)
    val buildingsDir = new File(getClass.getClassLoader.getResource(s"./layers/$layer/facility/building").getPath)
    buildingsDir.listFiles().toVector.map(f => {
      val t = decode[BuildingTemplateJson](fromFile(f).mkString).right.get
      val pres = t.producible.flatMap(res => producibles.find(x => x.name == res).map(x => x))
      val ores = t.obtainable.flatMap(res => obtainables.find(x => x.name == res).map(x => x))
      val bt = BuildingTemplate(t.name, pres, ores, Cost(t.cost), t.buildTime, t.shape, t.since)
      ShapeProvider.add(bt)
      bt
    })
  }
}

//TODO since
case class ExtractableTemplate(id: ItemId, name: String, weight: ItemWeight, baseVolume: Int, yieldTime: Long, since: String)
case class ObtainableTemplate(id: ItemId, name: String, weight: ItemWeight, yieldTime: Long, since: String)
case class ProducibleTemplate(id: ItemId, name: String, weight: ItemWeight, cost: Vector[ItemStack], yieldTime: Long, since: String)
sealed trait FacilityTemplate {
  val name: String
  val producibles: Vector[Producible]
  val obtainables: Vector[Obtainable]
  val cost: Cost
  val buildTime: Long
  val since: String
}
case class OutpostTemplate(name: String, extractable: String, obtainables: Vector[Obtainable], producibles: Vector[Producible], cost: Cost, buildTime: Long, since: String) extends FacilityTemplate
case class BuildingTemplate(name: String, producibles: Vector[Producible], obtainables: Vector[Obtainable], cost: Cost, buildTime: Long, shape: String, since: String) extends FacilityTemplate

case class OutpostTemplateJson(name: String, extractable: String, obtainable: Vector[String], cost: Vector[ItemStack], buildTime: Long, since: String)
case class BuildingTemplateJson(name: String, obtainable: Vector[String], producible: Vector[String], cost: Vector[ItemStack], buildTime: Long, shape: String, since: String)
