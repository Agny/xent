package ru.agny.xent

trait Action {
  type T

  def run(e: T): Either[Response, T]
}