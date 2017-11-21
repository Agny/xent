package ru.agny.xent.trade

case class BoardState(lots: Map[Long, Lot]) {
  def update(l: Lot): BoardState = BoardState(lots + (l.id -> l))
}
object BoardState {
  val empty = BoardState(Map.empty)
}