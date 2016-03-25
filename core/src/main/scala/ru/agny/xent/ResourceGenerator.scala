package ru.agny.xent

import java.io.File

import ru.agny.xent.utils.IdGen

import scala.util.Random

object ResourceGenerator {
  //get list of all possible resources for this layer
  def gen(layerLvl: Int): List[Extractable] = ResourceLoader.load(layerLvl.toString)

  def mbResource(seed: List[Extractable]): Option[Extractable] = {
    val threshold = 94
    Random.nextInt(100) match {
      case c if c > threshold => Some(chooseResource(seed))
      case _ => None
    }
  }

  private def chooseResource(from: List[Extractable]): Extractable = {
    val n = Random.nextInt(from.size)
    val chosen = from(n)
    chosen.copy(volume = randomVolume(chosen.volume))
  }

  private def randomVolume(base: Int): Int = (base * Random.nextDouble()).toInt
}

object ResourceLoader {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  implicit val formats = DefaultFormats
  val idGen = IdGen()

  //load resource templates in path
  def load(path: String): List[Extractable] = {
    val resourcesDir = new File(getClass.getResource(s"/layers/$path/resources").toURI)
    val s = resourcesDir.listFiles().toList
    s.map(f => parse(io.Source.fromFile(f).mkString).extract[ResourceTemplate]).map(r => fromTemplate(r))
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
