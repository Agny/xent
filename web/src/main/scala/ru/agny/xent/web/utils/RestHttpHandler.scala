package ru.agny.xent.web.utils

import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import ru.agny.xent.{LayerRuntime, MessageHandler}

/*
* Temporary handler for test purposes
* */
case class RestHttpHandler(handler: MessageHandler, runtime: LayerRuntime) extends SimpleChannelInboundHandler[FullHttpRequest] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    val buf = Unpooled.copiedBuffer(getResponse(msg, runtime))
    val resp = new DefaultFullHttpResponse(msg.protocolVersion, HttpResponseStatus.OK, buf)
    resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")
    val keepAlive = HttpUtil.isKeepAlive(msg)
    if (keepAlive) {
      resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes())
        .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
    }
    ctx.write(resp)
    val future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def getResponse(in: FullHttpRequest, runtime:LayerRuntime): Array[Byte] = {
    val rez = in.method() match {
      case HttpMethod.GET =>
        val layers = runtime.get
        JsonOps.fromLayer(layers.head).getBytes("UTF-8")
      case HttpMethod.POST =>
        val value = JsonOps.toMessage(in.content().toString())
        val ack = handler.sendTest(value)
        ack.value.getBytes("UTF-8")
      case _ => "none".getBytes("UTF-8")
    }
    rez
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close
  }
}
