package chatTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ChatClient2 {

    public static void main(String[] args) throws Exception{
        new ChatClient2("localhost", 8080).run();
    }

    private final String host;
    private final int port;

    public ChatClient2(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap  = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast("handler", new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                    System.out.println(msg);
                                }

                                @Override
                                public boolean acceptInboundMessage(Object msg) throws Exception {
                                    boolean b = super.acceptInboundMessage(msg);
                                    System.out.println("客户端2---acceptInboundMessage"+b);
                                    return b;
                                }

//                                @Override
//                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                    System.out.println("客户端2---channelRead");
//                                }

                                @Override
                                public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("客户端2---channelRegistered");
                                }

                                @Override
                                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("客户端2---channelUnregistered");
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("客户端2---channelActive");
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("客户端2---channelInactive");
                                }

                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("客户端2---channelReadComplete");
                                }

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    System.out.println("客户端2---userEventTriggered");
                                }

                                @Override
                                public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("客户端2---channelWritabilityChanged");
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    System.out.println("客户端2---exceptionCaught");
                                }

                                @Override
                                protected void ensureNotSharable() {
                                    System.out.println("客户端2---ensureNotSharable");
                                }

                                @Override
                                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("客户端2---handlerAdded");
                                }

                                @Override
                                public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("客户端2---handlerRemoved");
                                }
                            });
                        }
                    });
            Channel channel = bootstrap.connect(host, port).sync().channel();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                channel.writeAndFlush(in.readLine() + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }
}
