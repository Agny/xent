package ru.agny.xent.web

import java.net.InetSocketAddress

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.group.{ChannelGroup, DefaultChannelGroup}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.ssl.{SslContextBuilder, SslContext}
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.netty.util.concurrent.ImmediateEventExecutor
import ru.agny.xent.core.utils.LayerGenerator
import ru.agny.xent.{LayerRuntime, MessageQueue, MessageHandler}
import ru.agny.xent.web.utils.GameServerInitializer

case class GameServer(address: InetSocketAddress, context:SslContext) {
  val channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE)
  val eventGroup = new NioEventLoopGroup()

  def start() = {
    val bootstrap = new ServerBootstrap().group(eventGroup)
      .channel(classOf[NioServerSocketChannel]).childHandler(createInitializer(channelGroup))
    val future = bootstrap.bind(address)
    future.syncUninterruptibly()
  }

  def createInitializer(group: ChannelGroup) = {
    val queue = MessageQueue()
    val messageHandler = MessageHandler(queue)
    val layers = LayerRuntime.run(LayerGenerator.setupLayers(), queue)
    GameServerInitializer(channelGroup, context, messageHandler)
  }

  def destroy = {
    channelGroup.close()
    eventGroup.shutdownGracefully()
  }
}

object GameServer {

  def run = {
    val cert = new SelfSignedCertificate()
    val ctx = SslContextBuilder.forServer(cert.certificate(),cert.privateKey()).build()
    val endpoint = new GameServer(new InetSocketAddress(8888), ctx)
    val future = endpoint.start()
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        future.channel().close()
        endpoint.destroy
      }
    })
    future.channel().closeFuture().syncUninterruptibly()
  }
}

