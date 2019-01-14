package _10_2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 首先我们看 main 函数，它有两个参数：第一个是端口，第二个是 HTTP 服务端的 URL 路径。如果启动的时候没有配置，则使用默认值，默认端口是 8080，默认的 URL路径是 “/netty-guide/src/main/java“。
 *
 * 重点关注第 44~48 行，首先向 ChannelPipeline 中添加 HTTP 请求消息解码器，
 * 随后，又添加了 HttpObjectAggregator 解码器
 * 它的作用是将多个消息转换为单一的 FullHttpRequest 或者 FullHttpResponse，
 * 原因是 HTTP 解码器在每个 HTTP 消息中会生成多个消息对象。
 *
 *  (1) HttpRequest 1 HttpResponse;
 *
 *  (2) HttpContent;
 *
 *  (3) LastHttpContent。
 *
 * 第 43~46 行新增 HTTP 响应编码器，对 HTTP 响应消息进行编码；
 * 第 47 行新增 Chunked handler，
 * 它的主要作用是支持异步发送大的码流（例如大的文件传输），但不占用过多的内存，防止发生 Java 内存溢出错误。
 *
 * 最后添加 HttpFileServerHandler，用于文件服务器的业务逻辑处理。下面我们具体看看它是如何实现的。
 */
public class HttpFileServer {

    public static final String DEFAULT_URL = "/netty-guide/src/main/java";

    public void run (final int port, final String url){
        EventLoopGroup workGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("http-decoder",new HttpRequestDecoder())
                                .addLast("http-aggregator",new HttpObjectAggregator(65536))
                                .addLast("http-encoder",new HttpResponseEncoder())
                                .addLast("http-chunked",new ChunkedWriteHandler())
                                .addLast("fileServerHandler", new HttpFileServerHandler(url));

                    }
                });
        try {
            ChannelFuture future = serverBootstrap.bind("127.0.0.1",port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {

        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new HttpFileServer().run(8080,DEFAULT_URL);
    }
}
