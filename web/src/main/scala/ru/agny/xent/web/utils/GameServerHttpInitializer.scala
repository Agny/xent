package ru.agny.xent.web.utils

import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.ssl.{SslHandler, SslContext}
import io.netty.handler.stream.ChunkedWriteHandler
import ru.agny.xent.{LayerRuntime, MessageHandler}

case class GameServerHttpInitializer(context: SslContext, handler:MessageHandler, runtime:LayerRuntime) extends ChannelInitializer[Channel] {
  override def initChannel(ch: Channel): Unit = {
    val pipeline = ch.pipeline()
    //pipeline.addFirst(new SslHandler(context.newEngine(ch.alloc()))) Defold doesn't support https yet
    pipeline.addLast(new HttpServerCodec())
    pipeline.addLast(new ChunkedWriteHandler())
    pipeline.addLast(new HttpObjectAggregator(64 * 1024))
    pipeline.addLast(new RestHttpHandler(handler, runtime))
  }
}
