package ru.agny.xent.web.utils

import io.netty.channel.group.ChannelGroup
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketServerProtocolHandler}
import ru.agny.xent.web.MessageHandler

case class TextWebSocketFrameHandler(group: ChannelGroup, handler:MessageHandler) extends SimpleChannelInboundHandler[TextWebSocketFrame] {

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any): Unit = {
    if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
      ctx.pipeline().remove(classOf[HttpRequestHandler])
      group.add(ctx.channel())
    } else {
      super.userEventTriggered(ctx, evt)
    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame): Unit = {
    val value = JsonOps.toMessage(msg.text())
    val ack = handler.send(value, ctx.channel())
    ctx.channel().writeAndFlush(ack)
  }
}
