package ru.agny.xent.realm

/**
 * All coordinates are axial, see https://www.redblobgames.com/grids/hexagons/
 */
case class Coordinate(x: Int, y: Int) {

  def distance(to: Coordinate): Int =
    math.max(
      math.max(
        math.abs(this.x - to.x),
        math.abs(this.y - to.y)
      ),
      math.abs(this.z() - to.z())
    )

  def path(to: Coordinate): Path = Path(this, to)

  inline private def z(): Int = -x - y
}
