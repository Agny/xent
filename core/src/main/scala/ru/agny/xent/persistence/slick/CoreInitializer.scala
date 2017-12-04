package ru.agny.xent.persistence.slick

import DefaultProfile.api._
import DefaultProfile.db
import slick.jdbc.meta.MTable

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object CoreInitializer {

  private val toInit = Seq(
    UserEntity.table,
    ItemEntity.table,
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
