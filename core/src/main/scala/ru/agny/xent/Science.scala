package ru.agny.xent

case class Science(name: String, text: String, cost: Int, prev: Set[Science], next: Set[Science])
