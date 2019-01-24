package heartBeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 责任链模式：像netty的pipeline中的各个handler
 * @auther chen.haitao
 * @date 2019-01-24
 */
public class HeartBeatServer {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap
                    .group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //handler是针对与bossGroup
                    .handler(new LoggingHandler(LogLevel.ERROR))
                    //childHandler是针对与workerHandler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("IdleStateHandler",new IdleStateHandler(5,7,10, TimeUnit.SECONDS))
                                    //这里不继承SimpleChannelInboundHandler，
                                    .addLast("ChannelInboundHandlerAdapter", new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            if(evt instanceof IdleStateEvent){
                                                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                                String eventType = null;
                                                /**
                                                 * 这里有关于读写状态的监测，readerIdleTime时间没有读时间，就会打印；以此类推
                                                 * 客户端随便用哪个，用之前那个聊天室的客户端{@link chatTest.ChatClient1}也行，因为能向服务端写数据
                                                 */
                                                switch (idleStateEvent.state()){
                                                    case READER_IDLE:eventType = "读空闲";break;
                                                    case WRITER_IDLE:eventType = "写空闲";break;
                                                    default: eventType = "读写空闲";
                                                }
                                                System.out.println(ctx.channel().remoteAddress()+"-超时事件-->"+eventType);
                                                ctx.channel().close();
                                            }
                                        }
                                    });
                        }
                    });
            ChannelFuture sync = bootstrap.bind(8899).sync();
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
