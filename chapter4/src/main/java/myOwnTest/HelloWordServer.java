package myOwnTest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class HelloWordServer {
    private int port;

    public HelloWordServer(int port) {
        this.port = port;
    }

    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();
//        server.group(bossGroup,workGroup)
//                .channel(NioServerSocketChannel.class)
//                .childHandler(new ServerChannelInitializer());
        server.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch){
                        ch.pipeline().addLast("decoder",new StringDecoder())
                                .addLast("encoder",new StringEncoder())
                                .addLast("handler",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg){
                                System.out.println(ctx.channel().remoteAddress()+"===>server: "+msg.toString());
                                if(msg.toString().endsWith("bye\r\n")){
                                    ctx.write ("分手就分手！bye!");
                                    ctx.flush();
                                    ctx.close();
                                }else {
                                    ctx.write("received your msg");
                                    ctx.flush();
                                }
                            }


                            @Override
                            public void channelWritabilityChanged(ChannelHandlerContext ctx){
                                System.out.println("服务端：channelWritabilityChanged ！！！");
                            }



                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                super.exceptionCaught(ctx, cause);
                                ctx.close();
                            }
                        });
                    }
                });


        try {
            ChannelFuture future = server.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        HelloWordServer server = new HelloWordServer(7788);
        server.start();
    }
}
