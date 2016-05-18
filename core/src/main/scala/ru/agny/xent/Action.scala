package ru.agny.xent

trait Action
case class Craft(f: Facility => Resource, user: User, amount: Int) extends Action