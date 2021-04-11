package ru.agny.xent.item

import ru.agny.xent.{ResourceName, TimeInterval, item}
import ru.agny.xent.realm.Progress

/**
 * Generic resource data
 *
 * Baseline for yield time depends on craft system i.e. "how much of this is needed to craft that"
 *
 * @param name      "Copper", "Wood" etc.
 * @param yieldTime time in seconds
 */
enum Resource(val name: ResourceName, val yieldTime: TimeInterval):
  case Copper extends Resource("Copper", 3600)
  case Coal extends Resource("Coal", 5400)
  case Iron extends Resource("Iron", 5400)
  case Leather extends Resource("Leather", 3600)
  case Wood extends Resource("Wood", 1800)
end Resource
