package myOwnTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

public class CustomClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public static void main(String[] args) throws Exception {

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch){
                            ch.pipeline()
                                    .addLast(new MessageToByteEncoder<CustomMsg>() {
                                        @Override
                                        protected void encode(ChannelHandlerContext ctx, CustomMsg msg, ByteBuf out) throws Exception {
                                            if(null == msg){
                                                throw new Exception("msg is null");
                                            }

                                            String body = msg.getBody();
                                            byte[] bodyBytes = body.getBytes(Charset.forName("utf-8"));
                                            out.writeByte(msg.getType());
                                            out.writeByte(msg.getFlag());
                                            out.writeInt(msg.getLength());
                                            out.writeBytes(bodyBytes);
                                        }})
                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            CustomMsg customMsg = new CustomMsg((byte)0xAB, (byte)0xCD, "Hello,Netty".length(), "Hello,Netty---useless info");
                                            ctx.writeAndFlush(customMsg);
                                        }
                                    });
                        }
                    });

            ChannelFuture future = b.connect(HOST, PORT).sync();
            future.channel().writeAndFlush("Hello Netty Server ,I am a common client");
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
