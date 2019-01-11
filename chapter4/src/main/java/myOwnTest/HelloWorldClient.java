package myOwnTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HelloWorldClient {
    private  int port;
    private  String address;

    public HelloWorldClient(int port,String address) {
        this.port = port;
        this.address = address;
    }

    public void start(){
        EventLoopGroup group = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();
//        bootstrap.group(group)
//                .channel(NioSocketChannel.class)
//                .handler(new ClientChannelInitializer());

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch){
                                    ch.pipeline().addLast("decoder",new StringDecoder())
                                            .addLast("encoder",new StringEncoder())
                                            .addLast("handler",new ChannelInboundHandlerAdapter(){
                                                    @Override
                                                    public void channelRead(ChannelHandlerContext ctx, Object msg){
                                                        System.out.println("server say : "+msg.toString());
                                                    }

                                                    @Override
                                                    public void channelActive(ChannelHandlerContext ctx){
                                                        System.out.println("Client is active");
                                                    }

                                                    @Override
                                                    public void channelInactive(ChannelHandlerContext ctx){
                                                        System.out.println("Client is close");
                                                    }

                                                    @Override
                                                    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                                                        System.out.println("channelRegistered");
                                                    }

                                                    @Override
                                                    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                                                        System.out.println("channelUnregistered");
                                                    }

                                                    @Override
                                                    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                                        System.out.println("channelReadComplete");
                                                    }

                                                            @Override
                                                    public void channelWritabilityChanged(ChannelHandlerContext ctx){
                                                        System.out.println("客户端：channelWritabilityChanged ！！！");
                                                    }
                                    });
                                }
        });

        try {
            Channel channel = bootstrap.connect(address,port).sync().channel();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            for(;;){
                String msg = reader.readLine();
                if(msg == null){
                    continue;
                }
                channel.writeAndFlush(msg + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        HelloWorldClient client = new HelloWorldClient(7788,"127.0.0.1");
        client.start();
    }

}
