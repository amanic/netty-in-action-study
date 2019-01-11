package myOwnTest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;

public class CustomServer {

    /**
     *
     * @param maxFrameLength 解码时，处理每个帧数据的最大长度
     * @param lengthFieldOffset 该帧数据中，存放该帧数据的长度的数据的起始位置
     * @param lengthFieldLength 记录该帧数据长度的字段本身的长度
     * @param lengthAdjustment 修改帧数据长度字段中定义的值，可以为负数
     * @param initialBytesToStrip 解析的时候需要跳过的字节数
     * @param failFast 为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异常
     */

    private static final int MAX_FRAME_LENGTH = 1024 * 1024;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int LENGTH_FIELD_OFFSET = 2;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int INITIAL_BYTES_TO_STRIP = 0;

    private int port;

    public CustomServer(int port) {
        this.port = port;
    }

    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap sbs = new ServerBootstrap().group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH,LENGTH_FIELD_LENGTH,LENGTH_FIELD_OFFSET,LENGTH_ADJUSTMENT,INITIAL_BYTES_TO_STRIP,false){

                                        //判断传送客户端传送过来的数据是否按照协议传输，头部信息的大小应该是 byte+byte+int = 1+1+4 = 6
                                        private static final int HEADER_SIZE = 6;

                                        private byte type;

                                        private byte flag;

                                        private int length;

                                        private String body;

                                        @Override
                                        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
                                            if (in == null) {
                                                return null;
                                            }
                                            if (in.readableBytes() < HEADER_SIZE) {
                                                throw new Exception("可读信息段比头部信息都小，你在逗我？");
                                            }
                                            System.out.println("可读信息长度为 "+in.readableBytes()+"，其中头部信息长度为 "+HEADER_SIZE);

                                            //注意在读的过程中，readIndex的指针也在移动
                                            type = in.readByte();

                                            flag = in.readByte();

                                            length = in.readInt();

                                            if (in.readableBytes() < length) {
                                                throw new Exception("body字段你告诉我长度是"+length+",但是真实情况是没有这么多，你又逗我？");
                                            }
                                            ByteBuf buf = in.readBytes(length);
                                            byte[] req = new byte[buf.readableBytes()];
                                            buf.readBytes(req);
                                            body = new String(req, "UTF-8");

                                            CustomMsg customMsg = new CustomMsg(type,flag,length,body);
                                            return customMsg;
                                        }
                                    })
                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        public void channelRead0(ChannelHandlerContext ctx, Object msg){
                                            if(msg instanceof CustomMsg) {
                                                CustomMsg customMsg = (CustomMsg)msg;
                                                System.out.println("Client->Server:"+ctx.channel().remoteAddress()+" send "+customMsg.getBody());
                                            }
                                        }
                                    });
                        }

                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口，开始接收进来的连接
            ChannelFuture future = sbs.bind(port).sync();
            System.out.println("Server start listen at " + port );
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new CustomServer(port).start();
    }
}
