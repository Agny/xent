package ru.agny.xent.realm

/**
  * Rectangular-like map of a realm with a center at (0,0)
  * @param maxX maximum X range (absolute value)
  * @param maxY maximum Y range (absolute value)
  */
class Map(
    maxX:Int,
    maxY:Int,
    staticObjects: collection.Map[Coordinate, Static],
    dynamicObjects: collection.mutable.Map[Coordinate, Dynamic],
) {
  val minX = -maxX
  val minY = -maxY
  

}