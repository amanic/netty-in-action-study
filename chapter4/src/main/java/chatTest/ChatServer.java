package chatTest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ChatServer {

    private int port;

    public ChatServer(int port) {
        this.port = port;
    }

    public void start(){
        //配置服务端的NIO线程组
        //实际上EventLoopGroup就是Reactor线程组
        //两个Reactor一个用于服务端接收客户端的连接，另一个用于进行SocketChannel的网络读写
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //ServerBootstrap对象是Netty用于启动NIO服务端的辅助启动类，目的是降低服务端开发的复杂度
            ServerBootstrap bootstrap = new ServerBootstrap();
            //Set the EventLoopGroup for the parent (acceptor) and the child (client).
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //回调请求
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println("客户端连接：" + socketChannel.remoteAddress());
                            //用户定义的ChannelInitailizer加入到这个channel的pipeline上面去，这个handler就可以用于处理当前这个channel上面的一些事件
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //ChannelPipeline类似于一个管道，管道中存放的是一系列对读取数据进行业务操作的ChannelHandler。

                            /**
                             * 发送的数据在管道里是无缝流动的，在数据量很大时，为了分割数据，采用以下几种方法
                             * 定长方法
                             * 固定分隔符
                             * 将消息分成消息体和消息头，在消息头中用一个数组说明消息体的长度
                             */
                            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast("handler",new ChatServerHandler());
                        }
                    })
                    //.localAddress(new InetSocketAddress(port))
                    //配置NioServerSocketChannel的TCP参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE,true);

            //绑定监听端口，调用sync同步阻塞方法等待绑定操作完成，完成后返回ChannelFuture类似于JDK中Future
            ChannelFuture future = bootstrap.bind(port).sync();

            System.out.println("服务器启动：");
            //使用sync方法进行阻塞，等待服务端链路关闭之后Main函数才退出
            future.channel().closeFuture().sync();
            System.out.println("服务器关闭：");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //优雅退出，释放线程池资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatServer(8080).start();
    }
}
