package ru.agny.xent.web

import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.util.ReferenceCountUtil

case class BasicHandler() extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    val in = msg.asInstanceOf[ByteBuf]
    try {
      while (in.isReadable()) {
        print(in.readByte())
      }
    } finally {
      ReferenceCountUtil.release(msg)
    }

  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
