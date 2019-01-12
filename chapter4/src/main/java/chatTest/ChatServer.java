package chatTest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

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
                            pipeline.addLast("handler",new SimpleChannelInboundHandler <String>(){
                                /**
                                 * A thread-safe Set Using ChannelGroup, you can categorize Channels into a
                                 * meaningful group. A closed Channel is automatically removed from the
                                 * collection,
                                 */
                                public ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                                /**
                                 * 每当从服务端收到新的客户端连接时，客户端的 Channel 存入 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
                                 */
                                @Override
                                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                                    Channel incoming = ctx.channel();

                                    // Broadcast a message to multiple Channels
                                    channels.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 加入\n");

                                    channels.add(ctx.channel());
                                }
                                /**
                                 * 每当从服务端收到客户端断开时，客户端的 Channel 自动从 ChannelGroup 列表中移除了，并通知列表中的其他客户端 Channel
                                 */
                                @Override
                                public void handlerRemoved(ChannelHandlerContext ctx) throws Exception { // (3)
                                    Channel incoming = ctx.channel();

                                    //将消息广播到多个Channel
                                    channels.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 离开\n");

                                    // 一个关闭的Channel将自动从ChannelGroup中移除，
                                    // so there is no need to do "channels.remove(ctx.channel());"
                                }
                                /**
                                 * 每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel。
                                 * 如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
                                 */
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception { // (4)
                                    Channel incoming = ctx.channel();
                                    for (Channel channel : channels) {
                                        if (channel != incoming) {
                                            channel.writeAndFlush("[" + incoming.remoteAddress() + "]" + s + "\n");
                                        } else {
                                            channel.writeAndFlush("[you]" + s + "\n");
                                        }
                                    }
                                }
                                /**
                                 * 服务端监听到客户端活动
                                 */
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
                                    Channel incoming = ctx.channel();
                                    System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "在线");
                                }
                                /**
                                 * 服务端监听到客户端不活动
                                 */
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
                                    Channel incoming = ctx.channel();
                                    System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "掉线");
                                }
                                /**
                                 * exceptionCaught() 事件处理方法是当出现 Throwable 对象才会被调用，
                                 * 即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
                                 * 在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉。
                                 * 然而这个方法的处理方式会在遇到不同异常的情况下有不同的实现，比如你可能想在关闭连接之前发送一个错误码的响应消息。
                                 */
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    Channel incoming = ctx.channel();
                                    System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "异常");
                                    // 当出现异常就关闭连接
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
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
