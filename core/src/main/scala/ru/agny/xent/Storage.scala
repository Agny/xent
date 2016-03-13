package ru.agny.xent

case class Storage(resources: List[(Int, Resource)]) {
  def spend(recipe: Recipe): Either[Error, Storage] = {
    val diff = recipe.cost.map(x => resources.find(y => y._2.name == x._2.name) match {
      case Some(v) => (v._1 - x._1, v._2)
      case None => (0 - x._1, x._2)
    })
    val balance = resources.map(x => recipe.cost.find(y => y._2.name == x._2.name) match {
      case Some(v) => (x._1 - v._1, x._2)
      case None => x
    })
    diff.find(x => x._1 < 0) match {
      case Some(v) => Left(Error(s"There isn't enough of ${v._2}. Needed ${0 - v._1} more"))
      case None => Right(Storage(balance))
    }
  }
}
