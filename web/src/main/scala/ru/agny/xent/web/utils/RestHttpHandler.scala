package ru.agny.xent.web.utils

import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import ru.agny.xent.core.{Cell, LayerRuntime}
import ru.agny.xent.web.MessageHandler

/*
* Temporary handler for test purposes
* */
case class RestHttpHandler(handler: MessageHandler, runtime: LayerRuntime) extends SimpleChannelInboundHandler[FullHttpRequest] {

  val initPath = "/init"
  val loadPath = "/load"

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

  private def getResponse(in: FullHttpRequest, runtime: LayerRuntime): Array[Byte] = {
    val uriDecoder = new QueryStringDecoder(in.uri())
    val rez = in.method() match {
      case HttpMethod.GET if in.uri().startsWith(loadPath) =>
        JsonOps.toString(loadMap(uriDecoder)).getBytes("UTF-8")
      case HttpMethod.GET if in.uri().startsWith(initPath) =>
        JsonOps.toString(initMap(uriDecoder)).getBytes("UTF-8")
      case HttpMethod.POST =>
        val msg = JsonOps.toMessage(in.content().toString())
        val ack = handler.sendTest(msg)
        ack.value.getBytes("UTF-8")
      case _ => "none".getBytes("UTF-8")
    }
    rez
  }

  private def loadMap(decoder: QueryStringDecoder): Vector[Cell] = {
    val x = decoder.parameters().get("x").get(0)
    val y = decoder.parameters().get("y").get(0)
    val userId = decoder.parameters().get("user").get(0)
    val layerId = decoder.parameters().get("layer").get(0)
    runtime.get.find(_.id == layerId) match {
      case Some(v) => v.map.view(x.toInt, y.toInt).filter(x => x.city.nonEmpty || x.resource.nonEmpty)
      case None => Vector.empty
    }
  }

  private def initMap(decoder: QueryStringDecoder) = {
    val userId = decoder.parameters().get("user").get(0).toLong
    val layerId = decoder.parameters().get("layer").get(0)
    runtime.get.find(_.id == layerId) match {
      case Some(v) =>
        v.users.find(u => u.id == userId) match {
          case Some(user) => (user.city.c.x, user.city.c.y, v.map.length)
          case None => (2, 4, v.map.length)
        }
      case None => (0, 0, -1)
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close
  }
}
