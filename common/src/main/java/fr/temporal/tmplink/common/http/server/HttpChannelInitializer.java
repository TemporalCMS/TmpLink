package fr.temporal.tmplink.common.http.server;

import fr.temporal.tmplink.common.TmpLinkPlugin;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final TmpLinkPlugin plugin;

  public HttpChannelInitializer(TmpLinkPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void initChannel(SocketChannel channel) {
    ChannelPipeline pipeline = channel.pipeline();
    pipeline.addLast("decoder", new HttpRequestDecoder());
    pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
    pipeline.addLast("encoder", new HttpResponseEncoder());
    pipeline.addLast("handler", new HttpHandler(this.plugin));
  }
}