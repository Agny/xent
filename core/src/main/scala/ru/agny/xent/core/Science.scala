package ru.agny.xent.core

case class Science(name: String, text: String, cost: Int, prev: Set[Science], next: Set[Science])
