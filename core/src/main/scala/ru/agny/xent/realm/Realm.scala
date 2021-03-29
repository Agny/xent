package ru.agny.xent.realm

import ru.agny.xent.Message.Event
import ru.agny.xent.Action.{Noop, Op}
import ru.agny.xent.*
import ru.agny.xent.realm.Realm.Timer

/**
 * Global realm state
 *
 * @param id  identifier
 * @param map realm map content
 */
case class Realm(
  id: RealmId,
  //    level: Level,
  map: GameMap
) {
  val timer = Timer(System.currentTimeMillis())

  def handle(events: Seq[Event]): Unit = {
    events.sortBy(_.timestamp).foldLeft(tick()) {
      (realm, event) =>
        event.action match {
          case Noop =>
          case Op(v) =>
        }
    }
  }

  private def tick(): Unit = map.tick(timer)

}

object Realm {

  val StaticTickPeriod: TimeInterval = 30

  case class Timer(now: Long) {
    private var lastTick = now
    private var acc = 0

    def tick(): Tick = {
      val updated = System.currentTimeMillis()
      val interval = ((updated - lastTick) / 1000).toInt
      lastTick = updated
      acc += interval
      if (acc >= StaticTickPeriod) {
        val r = Strong(interval, acc)
        acc = 0
        r
      }
      else Weak(interval)
    }
  }

  sealed trait Tick
  case class Weak(volume: TimeInterval) extends Tick
  case class Strong(volume: TimeInterval, accumulated: TimeInterval) extends Tick
}