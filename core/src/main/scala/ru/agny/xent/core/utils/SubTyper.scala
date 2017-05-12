package ru.agny.xent.core.utils

trait SubTyper[From, To] {
  def asSub(a: From): Option[To]
}
