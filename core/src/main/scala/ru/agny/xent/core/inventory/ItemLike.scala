package ru.agny.xent.core.inventory

trait ItemLike[To <: Item, From <: Item] {
  def cast(v: From): (Option[From], Option[To])
}

object ItemLike {
  object implicits {
    implicit def isNotSub[To <: Item, From <: Item]: ItemLike[To, From] = (v: From) => (Some(v), None)

    implicit def isSub[To <: Item, From <: To]: ItemLike[To, From] = (v: From) => (Some(v), Some(v))
  }
}