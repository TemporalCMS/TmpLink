package fr.temporal.tmplink.common.http.server;

import fr.temporal.tmplink.common.TmpLinkPlugin;
import fr.temporal.tmplink.common.utils.Hash;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private final TmpLinkPlugin plugin;

  public HttpHandler(TmpLinkPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
    String uri = request.uri();
    HttpMethod method = request.method();

    if (!uri.equals("/")) {
      close(ctx, writeResponse(HttpResponseStatus.NOT_FOUND, "Error: Not Found"));
      return;
    }

    if (method == HttpMethod.GET) {
      close(ctx, writeResponse(HttpResponseStatus.OK, "Status: OK"));
      return;
    }

    if (method == HttpMethod.POST) {
      if (!this.plugin.getConfig().isValid()) {
        close(ctx, writeResponse(HttpResponseStatus.SERVICE_UNAVAILABLE, "Error: Invalid configuration"));
        return;
      }

      String siteKeyHash = Hash.SHA_256.hash(this.plugin.getConfig().getSiteKey());

      if (!siteKeyHash.equals(request.headers().get("Authorization"))) {
        close(ctx, writeResponse(HttpResponseStatus.FORBIDDEN, "Error: Invalid authorization"));
        return;
      }

      this.plugin.fetch();

      close(ctx, writeResponse(HttpResponseStatus.OK, "Status: OK"));

      return;
    }

    close(ctx, writeResponse(HttpResponseStatus.METHOD_NOT_ALLOWED, "Error: Method Not Allowed"));
  }


  private FullHttpResponse writeResponse(HttpResponseStatus status, String content) {
    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
    response.content().writeBytes(content.getBytes(StandardCharsets.UTF_8));
    return response;
  }

  private void close(ChannelHandlerContext ctx, FullHttpResponse response) {
    ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }
}
