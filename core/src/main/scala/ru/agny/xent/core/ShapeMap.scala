package ru.agny.xent.core

case class ShapeMap(private val cellsMap: CellsMap[LocalCell]) {
  def filter(f: LocalCell => Boolean): Vector[Shape] = toShape(cellsMap.filter(f))

  def buildings(): Vector[Shape] = toShape(cellsMap.filter(_.building.nonEmpty))

  def isAvailable(s: Shape): Boolean = {
    val withBuildings = buildings().flatMap(x => x.core +: x.parts)
    (s.core +: s.parts).forall(c => !withBuildings.exists(b => b.x == c.x && b.y == c.y))
  }

  def update(c:LocalCell):ShapeMap = ShapeMap(cellsMap.update(c))

  private def toShape(cells: Vector[LocalCell]): Vector[Shape] = {
    cells.foldLeft(Vector.empty[Shape])((s, c) => c.building match {
      case Some(v) => v.shape.form(c) +: s
      case None => s
    })
  }
}