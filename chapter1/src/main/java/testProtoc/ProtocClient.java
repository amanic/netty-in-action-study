package testProtoc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * @auther chen.haitao
 * @date 2019-01-26
 */
public class ProtocClient {

    public static void main(String[] args) {

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
//                                .addLast(new ProtobufVarint32FrameDecoder())
//                                .addLast(new ProtobufDecoder(DataInfo.Student.getDefaultInstance()))
//                                .addLast(new ProtobufVarint32LengthFieldPrepender())
//                                .addLast(new ProtobufEncoder())
                                .addLast(new ByteArrayDecoder())
                                .addLast(new ByteArrayEncoder())
                                .addLast(new SimpleChannelInboundHandler<byte[]>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
                                        for (byte b : msg){
                                            System.out.print(b);
                                        }
                                        System.out.println();
//                                        ctx.channel().writeAndFlush(msg);
                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        DataInfo.Student student = DataInfo.Student.newBuilder()
                                                .setAge(1)
                                                .setName("bviya")
                                                .setAddress("地址")
                                                .build();
                                        ctx.channel().writeAndFlush(student.toByteArray());
                                    }
                                });
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect("localhost", 8899).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
}
