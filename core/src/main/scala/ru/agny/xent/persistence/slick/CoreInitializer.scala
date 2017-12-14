package ru.agny.xent.persistence.slick

import slick.jdbc.meta.MTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class CoreInitializer(configPath: String) {

  val db = Database.forConfig(configPath)

  private val toInit = Seq(
    UserEntity.table,
    ItemTemplateEntity.table,
    ItemStackEntity.table,
  )

  def init() = {
    Await.result(createIfNotExists(toInit), Duration.Inf)
    true
  }

  def createIfNotExists(toInit: Seq[TableQuery[_ <: Table[_]]]) = {
    val listTables = db.run(MTable.getTables)
    listTables.flatMap(x => {
      val inDatabase = x.map(_.name.name)
      toInit.filterNot(t => inDatabase.contains(t.baseTableRow.tableName)) match {
        case h :: t => db.run(t.foldLeft(h.schema)(_ ++ _.schema).create)
        case _ => Future.successful()
      }
    })
  }
}

object CoreInitializer {
  lazy val common = new CoreInitializer("db")
  lazy val test = new CoreInitializer("db-test")

  def forConfig(path: String): CoreInitializer = path match {
    case "db" => common
    case "db-test" => test
  }
}
