package ru.agny.xent.bench

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class VectorDiffUpdateComparison {

  val (xy, newValue) = (XY(2, 3), XY(1000, 1000))
  val data = (1 until 1000 toVector) map (x => (1 until 1000 toVector) map (XY(x, _)))
  val flatten = data.flatten

  @Benchmark
  def update(): Vector[Vector[XY]] = {
    val ys = data(xy.x)
    data.updated(xy.x, ys.updated(xy.y, newValue))
  }

  @Benchmark
  def diff(): Vector[XY] = {
    newValue +: flatten.diff(Vector(xy))
  }

}
