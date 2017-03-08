package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Property

trait Accessory extends Equippable

case object DefaultAccessory extends Accessory {
  override val name: String = "Default"
  override val attrs: Seq[Property] = Seq.empty
}
