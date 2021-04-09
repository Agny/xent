package ru.agny.xent

trait PlayerService {
  def isHostile(a: PlayerId, b: PlayerId): Boolean
}
