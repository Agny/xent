package ru.agny.xent.web.utils

import java.io.{File, RandomAccessFile}

import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, DefaultFileRegion, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.ssl.SslHandler
import io.netty.handler.stream.ChunkedNioFile

case class HttpRequestHandler(index: File, wsUri: String) extends SimpleChannelInboundHandler[FullHttpRequest] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    if (wsUri.equalsIgnoreCase(msg.uri)) {
      ctx.fireChannelRead(msg.retain())
    } else {
      if (HttpUtil.is100ContinueExpected(msg)) send100Continue(ctx)
      val file = new RandomAccessFile(index, "r")
      val resp = new DefaultHttpResponse(msg.protocolVersion, HttpResponseStatus.OK)
      resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
      val keepAlive = HttpUtil.isKeepAlive(msg)
      if (keepAlive) {
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length())
          .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
      }
      ctx.write(resp)
      if (ctx.pipeline().get(classOf[SslHandler]) == null) {
        ctx.write(new DefaultFileRegion(file.getChannel, 0, file.length()))
      } else {
        ctx.write(new ChunkedNioFile(file.getChannel))
      }
      val future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
      if (!keepAlive) {
        future.addListener(ChannelFutureListener.CLOSE)
      }
    }
  }

  def send100Continue(ctx: ChannelHandlerContext) = {
    val resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)
    ctx.writeAndFlush(resp)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close
  }
}
