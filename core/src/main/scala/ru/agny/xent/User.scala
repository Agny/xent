package ru.agny.xent

import ru.agny.xent.utils.IdGen

case class User(id: Long, name: String, storage: Storage) {
  val localIdGen = IdGen()
//  implicit var rates: Map[Facility, Int] = Map(() -> 1) //very stronk formula from current layer/science/etc

  def tick(actions: Seq[Action]): User = {
    val extractionOutput = storage.extract()
    val productionOutput = actions.collect { case a: Craft => a }.foldRight(extractionOutput)((x, y) => y.produce(x))
    User(id, name, productionOutput)
  }

  override def toString = s"id=$id;name=$name"
}
