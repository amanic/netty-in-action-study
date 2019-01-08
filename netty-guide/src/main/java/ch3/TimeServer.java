package ch3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TimeServer {

    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // TODO 请自行扩展.
            }
        }

        new TimeServer().bind(port);
    }

    public void bind(int port) {
        /**
         * 创建了两个 NioEventLoopGroup 实例。
         * NioEventLoopGroup 是个线程组，它包含了一组 NIO 线程，专门用于网络事件的处理，实际上它们就是 Reactor 线程组。
         * 这里创建两个的原因是一个用于服务端接受客户端的连接，另一个用于进行 SocketChannel 的网络读写。
         */
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            /**
             * 创建 ServerBootstrap 对象，它是 Netty 用于启动 NIO 服务端的辅助启动类，目的是降低服务端的开发复杂度
             */
            ServerBootstrap b = new ServerBootstrap();
            /**
             * 调用 ServerBootstrap 的 group 方法，将两个 NIO 线程组当作入参传递到 ServerBootstrap 中。
             * 接着设置创建的 Channel 为 NioServerSocketChannel，它的功能对应于 JDK NIO 类库中的 ServerSocketChannel 类。
             * 然后配置 NioServerSocketChannel的 TCP参数，此处将它的 backlog 设置为1024，
             * 最后绑定I/O事件的处理类ChildChannelHandler,它的作用类似于Reactor 模式中的handler类，主要用于处理网络I/O 事件，例如记录日志、对消息进行编解码等。
             */
            b.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1012)
                    .childHandler(new ChildChannelHandler());

            /**
             * 服务端启动辅助类配置完成之后，调用它的 bind 方法绑定监听端口，随后，调用它的同步阻塞方法 sync 等待绑定操作完成。
             * 完成之后 Netty 会返回一个 ChannelFuture，它的功能类似于 JDK 的 java. Util. Concurrent. Future，主要用于异步操作的通知回调。
             */
            ChannelFuture f = b.bind(port).sync();     // 绑定端口
            f.channel().closeFuture().sync();// 等待服务器端监听端口关闭

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 优雅退出，并释放线程池资源
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new TimeServerHandler());
        }
    }
}