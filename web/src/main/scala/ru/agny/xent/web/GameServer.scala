package ru.agny.xent.web

import java.net.InetSocketAddress
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.group.{ChannelGroup, DefaultChannelGroup}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.{NioDatagramChannel, NioServerSocketChannel}
import io.netty.handler.ssl.{SslContext, SslContextBuilder}
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.netty.util.concurrent.ImmediateEventExecutor
import ru.agny.xent.web.utils.{GameServerHttpInitializer, GameServerInitializer}

case class GameServer(address: InetSocketAddress, context:SslContext) {
  val channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE)
  val eventGroup = new NioEventLoopGroup()

  def start() = {
    val bootstrap = new ServerBootstrap().group(eventGroup)
      .channel(classOf[NioServerSocketChannel]).childHandler(createInitializer(channelGroup))
    val future = bootstrap.bind(address)
    println(s"Socket listener at $address")
    future.syncUninterruptibly()
  }

  def createInitializer(group: ChannelGroup) = {
//    val queue = MessageQueue.global
//    val messageHandler = MessageHandler(queue)
//    val runtime = LayerRuntime.run(LayerGenerator.setupLayers(), queue)
    //    GameServerHttpInitializer(context, messageHandler, runtime)
    GameServerInitializer(channelGroup, context)
  }

  def destroy = {
    channelGroup.close()
    eventGroup.shutdownGracefully()
  }
}

object GameServer {
  val port = 8052

  def run = {
    val cert = new SelfSignedCertificate()
    val ctx = SslContextBuilder.forServer(cert.certificate(),cert.privateKey()).build()
    val endpoint = new GameServer(new InetSocketAddress(port), ctx)
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

