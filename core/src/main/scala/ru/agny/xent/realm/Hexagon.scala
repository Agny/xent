package ru.agny.xent.realm

import ru.agny.xent.Distance

/**
 * All coordinates are axial, see https://www.redblobgames.com/grids/hexagons/
 */
case class Hexagon(x: Int, y: Int) {

  def distance(to: Hexagon): Int =
    math.max(
      math.max(
        math.abs(this.x - to.x),
        math.abs(this.y - to.y)
      ),
      math.abs(this.z() - to.z())
    )

  def path(to: Hexagon): Path = Path(this, to)

  inline private def z(): Int = -x - y
}

object Hexagon {
  /**
   * Length from side to center * 2
   * It should be something relatable to a general idea of 
   * "minimum 3 tiles between players & no less than 1 hour for incoming attack"
   */
  val Center: Distance = 60000
  val Length: Distance = Center * 2
}
