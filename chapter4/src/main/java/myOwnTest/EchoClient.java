package myOwnTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.util.Date;


public class EchoClient {

    private final  int port;

    public EchoClient(int port) {
        this.port = port;
    }

    public static void main(String[] argsw) {
        try {
            new EchoClient(8080).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup).
                    channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>(){
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("channelActive"+new Date().getTime());
                                    ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
                                }

                                public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    ByteBuf in = msg;
                                    System.out.println("读取服务端channelRead0="+in.toString(CharsetUtil.UTF_8)+ new Date().getTime());
                                }

                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    cause.printStackTrace();
                                    ctx.close();
                                }


                                @Override
                                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("handlerAdded"+ new Date().getTime());
                                }

                                @Override
                                public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("handlerRemoved"+ new Date().getTime());
                                }
                            });
                        }
                    });
            //异步连接远程服务，连接远程服务成功后，输出"已经连接到服务器！"
            ChannelFuture f = b.connect("127.0.0.1",port).sync();
//            f.addListener(new GenericFutureListener<Future<? super Void>>() {
//                @Override
//                public void operationComplete(Future<? super Void> future) throws Exception {
//                    if (future.isSuccess()) {
//                        System.out.println("已经连接到服务器！");
//                        ByteBuf byteBuf = Unpooled.copiedBuffer("创建ByteBuf", CharsetUtil.UTF_8);
//                        ChannelFuture channelFuture = f.channel().writeAndFlush(byteBuf);
//                    }else {
//                        Throwable throwable = future.cause();
//                        throwable.printStackTrace();
//                    }
//                }
//            });
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }
}
