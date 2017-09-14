package ru.agny.xent.core.inventory

trait ItemLike[To <: Item, From <: Item] {
  def cast(v: From): (Option[From], Option[To])
}

object ItemLike {
  object implicits {
    implicit def isNotSub[To <: Item, From <: Item]: ItemLike[To, From] = new ItemLike[To, From] {
      override def cast(v: From): (Option[From], Option[To]) = (Some(v), None)
    }

    implicit def isSub[To <: Item, From <: To]: ItemLike[To, From] = new ItemLike[To, From] {
      override def cast(v: From): (Option[From], Option[To]) = (Some(v), Some(v))
    }
  }
}