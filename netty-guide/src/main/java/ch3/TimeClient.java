package ch3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TimeClient {

    public void connect(int port, String host) {
        //配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();

        /**
         * 创建客户端辅助启动类 Bootstrap，随后需要对其进行配置。与服务端不同的是，它的 Channel 需要设置为 NioSocketChannel，(服务端设置为NioServerSocketChannel)
         * 然后为其添加 handler，此处为了简单直接创建匿名内部类，实现 initChannel 方法，其作用是当创建 NioSocketChannel 成功之后，在初始化它的时候将它的 ChannelHandler 设置到 ChannelPipeline中，用于处理网络I/O 事件。
         *
         * 客户端启动辅助类设置完成之后，调用 connect 方法发起异步连接，然后调用同步方法等待连接成功。
         *
         * 最后，当客户端连接关闭之后，客户端主函数退出，在退出之前，释放 NIO 线程组的资源。
         */
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TimeClientHandler());
                    }
                });


        try {
            ChannelFuture f = b.connect(host, port).sync(); // 发起异步连接操作
            f.channel().closeFuture().sync();// 等待客户端链路关闭。
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // TODO 请自行扩展.
            }
        }

        new TimeClient().connect(port, "127.0.0.1");
    }
}