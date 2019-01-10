package myOwnTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


public class HWClient {
    private  int port;
    private  String address;

    public HWClient(int port, String address) {
        this.port = port;
        this.address = address;
    }

    public void start(){
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("decoder",new StringDecoder())
                                .addLast("encoder",new StringEncoder())
                                .addLast("handler1",new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg){
                                        System.out.println("server say : "+msg.toString());
                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx){
                                        System.out.println("Handler1");
                                        ctx.fireChannelActive();
                                    }

                                    @Override
                                    public void channelInactive(ChannelHandlerContext ctx){
                                        System.out.println("Client is close");
                                    }
                                })
                                .addLast("handler2",new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx){
                                        System.out.println("Handler2");
                                    }


                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
                                        ctx.close();
                                    }
                                });
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect(address,port).sync();
            future.channel().writeAndFlush("Hello Netty Server ,I am a common client");
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        HWClient client = new HWClient(7788,"127.0.0.1");
        client.start();
    }
}
