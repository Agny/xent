package ru.agny.xent.web

import java.net.InetSocketAddress

object Basic extends App {
  val endpoint = new GameServer(new InetSocketAddress(8888))
  endpoint.run
}