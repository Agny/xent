package ru.agny.xent

case class User(name: String) {
  val copper = Extractable("copper", 10, Set.empty)
  val stannum = Extractable("stannum", 5, Set.empty)
  val brass = Producible("brass", Recipe(Map(2 -> copper, 1 -> stannum)), Set.empty)
  val brassBracelet = Producible("brassBracelet", Recipe(Map(4 -> brass, 2 -> copper)), Set.empty)
  val facility1 = Outpost("copper camp", 1, copper, Set.empty)
  val facility2 = Outpost("stannum camp", 2, stannum, Set.empty)
  val facility3 = Building("old forge", 3, List(brass, brassBracelet), Set.empty)
  private val facilities = List(facility1, facility2, facility3)
  private var storage = Storage(List((3, copper), (1, stannum), (3, brass)))
  private val productionRates = facilities.zip(List(1, 1, 1)).toMap

  def tick() = {
    val z = facilities.collect { case x: Outpost => x }.map(x => x.produce(productionRates(x))(x.resource.name)).collect { case x: Right[Nothing, (Int, Resource)] => x.right.get }
    val updated = storage.resources.map(x =>
      z.find(y => x._2.name == y._2.name) match {
        case Some(v) => (x._1 + v._1, v._2)
        case None => x
      }
    )
    storage = Storage(updated ::: z.filter(x => !updated.exists(y => x._2.name == y._2.name)))

    storage.spend(brassBracelet.recipe) match {
      case Right(v) => storage = v
      case Left(v) => println(v)
    }

    val z2 = facilities.collect { case x: Building => x }.map(x => x.produce(productionRates(x))(brassBracelet.name)).collect { case x: Right[Nothing, (Int, Resource)] => x.right.get }
    val updated2 = storage.resources.map(x =>
      z2.find(y => x._2.name == y._2.name) match {
        case Some(v) => (x._1 + v._1, v._2)
        case None => x
      }
    )
    Storage(updated2 ::: z2.filter(x => !updated2.exists(y => x._2.name == y._2.name)))
  }

  def tack(storage: Storage): Storage = {
    ???
  }

}
