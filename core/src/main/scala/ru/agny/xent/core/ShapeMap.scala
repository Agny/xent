package ru.agny.xent.core

case class ShapeMap(private val cellsMap: CellsMap[LocalCell]) {
  def filter(f: LocalCell => Boolean): Seq[Shape] = toShape(cellsMap.filter(f))

  def buildings(): Seq[Shape] = toShape(cellsMap.filter(_.building.nonEmpty))

  def find(c: Cell): Shape = toShape(
    cellsMap.flatMap((x) => cellsMap.find(c))
  ).head

  def isAvailable(s: Shape): Boolean = ???

  private def toShape(cells: Seq[LocalCell]): Seq[Shape] = ???

}