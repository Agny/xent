package ru.agny.xent.trade

import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import ru.agny.xent.core.Layer.LayerId

class Board(layer: LayerId) extends PersistentActor {

  var state: BoardState = BoardState.empty

  def update(v: Lot): Unit = {
    state = state.update(v)
  }

  override val receiveRecover: Receive = {
    case l: Lot => update(l)
    case SnapshotOffer(_, s: BoardState) =>
      println(s"offered state = $s")
      state = s
  }

  override val receiveCommand: Receive = {
    case l: Lot =>
      persist(l) { ev =>
        update(ev)
        context.system.eventStream.publish(ev)
      }
    case SaveSnapshotSuccess(metadata) =>
      println(s"SaveSnapshotSuccess(metadata) :  metadata=$metadata")
    case SaveSnapshotFailure(metadata, reason) =>
      println(
        s"""SaveSnapshotFailure(metadata, reason) :
        metadata=$metadata, reason=$reason""")
    case "print" => println(state)
    case "snap" => saveSnapshot(state)
    case "boom" => throw new UnsupportedOperationException("sup")

  }

  override val persistenceId = s"board-$layer"
}
