package ru.agny.xent

case class Recipe(title:String, src: Map[Resource, Cost], since: Prereq*) {
  def produce(): Either[Error, List[(Resource, ResourceUnit)]] = {
    val res = src.map(x => x._1.out(x._2))
    res.find(x => x.isLeft).getOrElse(Right(res.flatMap(x => x.fold(fa => List.empty, fb => fb)).toList))
  }
}
