package ru.agny.xent.web

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{ChannelOption, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

object Basic extends App {
  val bossGroup = new NioEventLoopGroup()
  val workerGroup = new NioEventLoopGroup()
  val b = new ServerBootstrap()
  b.group(bossGroup, workerGroup).channel(classOf[NioServerSocketChannel])
  .childHandler(new ChannelInitializer[SocketChannel] {
    override def initChannel(ch: SocketChannel): Unit = {
      ch.pipeline().addLast(BasicHandler())
    }
  }).option[Integer](ChannelOption.SO_BACKLOG, 128).childOption[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)

  val f = b.bind(8888).sync()
  f.channel().closeFuture().sync()
}