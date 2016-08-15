package ru.agny.xent.web

import java.io.{RandomAccessFile, File}
import java.net.InetSocketAddress

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.group.{ChannelGroup, DefaultChannelGroup}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.{WebSocketServerProtocolHandler, TextWebSocketFrame}
import io.netty.handler.ssl.SslHandler
import io.netty.handler.stream.{ChunkedNioFile, ChunkedWriteHandler}
import io.netty.util.concurrent.ImmediateEventExecutor

case class GameServer(address: InetSocketAddress) {
  val channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE)
  val eventGroup = new NioEventLoopGroup()

  def start() = {
    val bootstrap = new ServerBootstrap().group(eventGroup)
      .channel(classOf[NioServerSocketChannel]).childHandler(createInitializer(channelGroup))
    val future = bootstrap.bind(address)
    future.syncUninterruptibly()
  }

  def createInitializer(group: ChannelGroup) = GameServerInitializer(channelGroup)

  def run = {
    val endpoint = new GameServer(new InetSocketAddress(8888))
    val future = endpoint.start()
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        future.channel().close()
        endpoint.destroy
      }
    })
    future.channel().closeFuture().syncUninterruptibly()
  }

  def destroy = {
    channelGroup.close()
    eventGroup.shutdownGracefully()
  }
}

case class GameServerInitializer(group: ChannelGroup) extends ChannelInitializer[Channel] {
  override def initChannel(ch: Channel): Unit = {
    val pipeline = ch.pipeline()
    pipeline.addLast(new HttpServerCodec())
    pipeline.addLast(new ChunkedWriteHandler())
    pipeline.addLast(new HttpObjectAggregator(64 * 1024))
    pipeline.addLast(new HttpRequestHandler(indexFile, "/ws"))
    pipeline.addLast(new WebSocketServerProtocolHandler("/ws"))
    pipeline.addLast(new TextWebSocketFrameHandler(group))
  }

  val indexFile = new File("pathtoindex")
}

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

case class TextWebSocketFrameHandler(group: ChannelGroup) extends SimpleChannelInboundHandler[TextWebSocketFrame] {

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any): Unit = {
    if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
      ctx.pipeline().remove(classOf[HttpRequestHandler])
      group.writeAndFlush(new TextWebSocketFrame("Joined"))
      group.add(ctx.channel())
    } else {
      super.userEventTriggered(ctx, evt)
    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame): Unit = {
    group.writeAndFlush(msg.retain())
  }
}
