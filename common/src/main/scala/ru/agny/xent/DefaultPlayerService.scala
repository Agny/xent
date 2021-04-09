package ru.agny.xent

class DefaultPlayerService extends PlayerService {
  override def isHostile(a: PlayerId, b: PlayerId) = a != b
}
