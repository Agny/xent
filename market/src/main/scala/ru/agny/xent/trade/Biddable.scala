package ru.agny.xent.trade

trait Biddable extends Lot {
  val lastBid: Option[Bid]
}
