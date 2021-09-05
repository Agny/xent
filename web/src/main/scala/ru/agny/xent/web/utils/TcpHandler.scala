package ru.agny.xent.web.utils

import io.netty.buffer.ByteBuf
import io.netty.channel.group.ChannelGroup
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketServerProtocolHandler}
import io.netty.util.ByteProcessor

import java.io.{InputStream, OutputStream}
import java.nio.{ByteBuffer, ByteOrder}
import java.nio.channels.{FileChannel, GatheringByteChannel, ScatteringByteChannel}
import java.nio.charset.Charset

case class TcpHandler(group: ChannelGroup) extends SimpleChannelInboundHandler[ByteBuf] {

  /*override def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any): Unit = {
    if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
      ctx.pipeline().remove(classOf[HttpRequestHandler])
      group.add(ctx.channel())
    } else {
      super.userEventTriggered(ctx, evt)
    }
  }*/

  override def channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf): Unit = {
    val msgString = msg.toString(Charset.forName("UTF-8")) //TODO here comes json/whatever
    println(msgString)
  }
}
