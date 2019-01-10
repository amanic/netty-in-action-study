package myOwnTest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class AboutPackageServer {

    private int port;

    public AboutPackageServer(int port) {
        this.port = port;
    }

    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                //添加下面一行可以解决拆包粘包问题
//                                .addLast(new LineBasedFrameDecoder(2048))
                                .addLast("encoder",new StringEncoder())
                                .addLast("decoder",new StringDecoder())
                                .addLast("handler",new ChannelInboundHandlerAdapter(){
                                    private int counter;

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg){
                                        String body = (String)msg;
                                        System.out.println("server receive order : " + body + ";the counter is: " + ++counter);
                                        ctx.writeAndFlush(body);
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        super.exceptionCaught(ctx, cause);
                                    }
                                });
                    }
                });


        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        AboutPackageServer server = new AboutPackageServer(7788);
        server.start();
    }
}
