package myOwnTest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class EchoServer {

    private final  int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {

        int port = 8080;

        new EchoServer(port).start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup).
                    channel(NioServerSocketChannel.class).//指定channel使用Nio传输
                    localAddress(new InetSocketAddress(port)).//执行端口设置套接字地址
                    childHandler(new ChannelInitializer<SocketChannel>() {//添加echoServerHandler到Channel的channelpipeline上
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline channelPipeline = socketChannel.pipeline();
                    channelPipeline.addFirst(new ChannelInboundHandlerAdapter(){
                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("Other注册事件");
                            ctx.fireChannelRegistered();
                        }

                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("Other取消注册事件");
                            ctx.fireChannelUnregistered();
                        }

                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("Other有新客户端连接接入。。。"+ctx.channel().remoteAddress());
                            ctx.fireChannelActive();
                        }

                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("Other失去连接");
                            ctx.fireChannelInactive();
                        }

                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            ByteBuf in = (ByteBuf) msg;
                            System.out.println("Other读客户端传入数据="+in.toString(CharsetUtil.UTF_8));
                            final ByteBuf byteBuf = Unpooled.copiedBuffer("Other channelRead Netty rocks!", CharsetUtil.UTF_8);
                            ctx.writeAndFlush(byteBuf);
                            ctx.fireChannelRead(msg);
                            //ReferenceCountUtil.release(msg);
                        }

                        public void channelReadComplete(ChannelHandlerContext ctx){
                            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {
                                    if (future.isSuccess()) {
                                        System.out.println("Other执行成功="+future.isSuccess());
                                    }
                                }
                            });
                            final ByteBuf byteBuf = Unpooled.copiedBuffer("Other channelReadComplete Netty rocks!", CharsetUtil.UTF_8);
                            ctx.writeAndFlush(byteBuf).addListener(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {
                                    if (future.isSuccess())  {
                                    }else {

                                    }
                                    ReferenceCountUtil.release(byteBuf);
                                }
                            });
                            ctx.fireChannelReadComplete();
                        }

                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            System.out.println("Other  userEventTriggered");
                        }

                        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("Other  channelWritabilityChanged");
                        }

                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                            cause.printStackTrace();
                            ctx.close();
                        }
                    });
                    channelPipeline.addFirst(new ChannelOutboundHandlerAdapter(){
                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                            System.out.println("EchoServerOutHandler   write "+((ByteBuf)msg).toString(Charset.defaultCharset()));
                            ctx.write(msg, promise);
                        }
                    });
                    channelPipeline.addLast(new ChannelInboundHandlerAdapter(){
                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("注册事件");
                        }

                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("取消注册事件");
                        }

                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("有新客户端连接接入。。。"+ctx.channel().remoteAddress());
                        }

                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("失去连接");
                        }

                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            ByteBuf in = (ByteBuf) msg;
                            System.out.println("读客户端传入数据="+in.toString(CharsetUtil.UTF_8));
                            ctx.writeAndFlush(Unpooled.copiedBuffer("channelRead Netty rocks!", CharsetUtil.UTF_8));
                            //ctx.fireChannelActive();
                        }

                        public void channelReadComplete(ChannelHandlerContext ctx){
                            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {
                                    if (future.isSuccess()) {
                                        System.out.println("执行成功="+future.isSuccess());
                                    }
                                }
                            });
                            ctx.writeAndFlush(Unpooled.copiedBuffer("channelReadComplete Netty rocks!", CharsetUtil.UTF_8)).addListener(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {
                                    if (future.isSuccess())  {

                                    }else {

                                    }
                                }
                            });
                        }

                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            System.out.println("userEventTriggered");
                        }

                        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("channelWritabilityChanged");
                        }

                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                            cause.printStackTrace();
                            ctx.close();
                        }
                    });
                }
            });
            ChannelFuture f = serverBootstrap.bind().sync();//异步绑定服务器，调用sync()方法阻塞等待直到绑定完成
            f.channel().closeFuture().sync();//获得Channel的closefutrue，并且阻塞当前线程直到它完成
        } catch (InterruptedException e) {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }
}
