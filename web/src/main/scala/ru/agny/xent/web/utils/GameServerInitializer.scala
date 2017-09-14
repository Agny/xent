package ru.agny.xent.web.utils

import java.io.File

import io.netty.channel.group.ChannelGroup
import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.ssl.{SslContext, SslHandler}
import io.netty.handler.stream.ChunkedWriteHandler
import ru.agny.xent.web.MessageHandler

case class GameServerInitializer(group: ChannelGroup, context: SslContext, handler:MessageHandler) extends ChannelInitializer[Channel] {
  override def initChannel(ch: Channel): Unit = {
    val pipeline = ch.pipeline()
    pipeline.addFirst(new SslHandler(context.newEngine(ch.alloc())))
    pipeline.addLast(new HttpServerCodec())
    pipeline.addLast(new ChunkedWriteHandler())
    pipeline.addLast(new HttpObjectAggregator(64 * 1024))
    pipeline.addLast(new HttpRequestHandler(indexFile, "/ws"))
    pipeline.addLast(new WebSocketServerProtocolHandler("/ws"))
    pipeline.addLast(new TextWebSocketFrameHandler(group, handler))
  }

  val indexFile = new File(getClass.getClassLoader.getResource("test.html").toURI)
}
