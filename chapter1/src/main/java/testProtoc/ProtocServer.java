package testProtoc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @auther chen.haitao
 * @date 2019-01-26
 */
public class ProtocServer {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.ERROR))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
//                                .addLast(new ProtobufVarint32FrameDecoder())
//                        .addLast(new ProtobufDecoder(DataInfo.Student.getDefaultInstance()))
//                        .addLast(new ProtobufVarint32LengthFieldPrepender())
//                        .addLast(new ProtobufEncoder())
                                .addLast(new ByteArrayDecoder())
                                .addLast(new ByteArrayEncoder())
                        .addLast(new SimpleChannelInboundHandler<byte[]>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
                                for (byte b : msg){
                                    System.out.print(b);
                                }
                                System.out.println();
                                ctx.channel().writeAndFlush(msg);
                            }
                        });
                    }
                });

        ChannelFuture future = serverBootstrap.bind(8899).sync();
        future.channel().closeFuture().sync();

    }
}
