package first;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;

/**
 * @auther chen.haitao
 * @date 2019-01-23
 * 浏览器访问，返回helloworld
 */
public class TestServer {

    public static void main(String[] args) {
        /**
         * 只用一个线程组也能完成工作。事件循环组，可以理解为一个死循环，不断接受连接
         * 上面那个是不断接收连接，下面那个是对连接进行业务处理
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap
                    .group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        /**
                         * 连接被注册之后，channel就被创建，然后执行该方法。也可以说是一个回调方法。
                         */
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("HttpServerCodec",new HttpServerCodec())
                            .addLast("SimpleChannelInboundHandler", new SimpleChannelInboundHandler<HttpObject>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                                    //一个请求这里会有两个打印，所以下面要加个判断，不然会报错
                                    System.out.println(msg.getClass());

                                    System.out.println(ctx.channel().remoteAddress());

                                    if(msg instanceof HttpRequest){

                                        System.out.println("请求方法名："+((HttpRequest) msg).method());

                                        System.out.println("uri="+new URI(((HttpRequest) msg).uri()).getPath());

                                        ByteBuf buf = Unpooled.copiedBuffer("陈海涛你好！", CharsetUtil.UTF_8);
                                        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                HttpResponseStatus.OK, buf);
                                        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain");
                                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,buf.readableBytes());
                                        ctx.writeAndFlush(response);
                                        // 下面这行代码针对浏览器
                                        ctx.channel().close();
                                    }
                                }
                            });
                        }
                    });
            ChannelFuture sync = bootstrap.bind(8899).sync();
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
