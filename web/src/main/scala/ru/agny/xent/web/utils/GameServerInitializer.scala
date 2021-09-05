package ru.agny.xent.web.utils

import java.io.File

import io.netty.channel.group.ChannelGroup
import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.ssl.{SslContext, SslHandler}
import io.netty.handler.stream.ChunkedWriteHandler

case class GameServerInitializer(group: ChannelGroup, context: SslContext) extends ChannelInitializer[Channel] {
  override def initChannel(ch: Channel): Unit = {
    val pipeline = ch.pipeline()
    /*
    pipeline.addFirst(new SslHandler(context.newEngine(ch.alloc()))) //TODO think about ssl/tls?

    //TODO have to distinguish between raw tcp and http when/if needs arises
    pipeline.addLast(new HttpServerCodec())
    pipeline.addLast(new HttpObjectAggregator(64 * 1024))
    pipeline.addLast(HttpRequestHandler(indexFile, "/ws"))
    pipeline.addLast(new WebSocketServerProtocolHandler("/"))
    */
    pipeline.addLast(TcpHandler(group))
  }

  val indexFile = new File(getClass.getClassLoader.getResource("test.html").toURI)
}
