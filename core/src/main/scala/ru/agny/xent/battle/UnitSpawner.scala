package ru.agny.xent.battle

import ru.agny.xent.core.utils.SelfAware

trait UnitSpawner extends MapObject {
  this: SelfAware =>

  def spawn: (UnitSpawner, MapObject)
}
