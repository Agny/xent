package ru.agny.xent.core

case class ShapeMap(private val cellsMap: CellsMap[LocalCell]) {
  def filter(f: LocalCell => Boolean): Seq[Shape] = toShape(cellsMap.filter(f))

  def buildings(): Seq[Shape] = toShape(cellsMap.filter(_.building.nonEmpty))

  def isAvailable(s: Shape): Boolean = {
    val withBuildings = buildings().flatMap(x => x.parts :+ x.core)
    (s.parts :+ s.core).forall(c => !withBuildings.exists(b => b.x == c.x && b.y == c.y))
  }

  def update(c:LocalCell):ShapeMap = ShapeMap(cellsMap.update(c))

  private def toShape(cells: Seq[LocalCell]): Seq[Shape] = {
    cells.foldLeft(Seq.empty[Shape])((s, c) => c.building match {
      case Some(v) => s :+ v.shape.form(c)
      case None => s
    })
  }
}