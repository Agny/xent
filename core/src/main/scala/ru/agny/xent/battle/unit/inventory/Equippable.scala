package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Property

trait Equippable {
  val name: String
  val attrs: Seq[Property]
}