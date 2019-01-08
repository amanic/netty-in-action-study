package ch3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * TimeServerHandler 继承自 ChannelHandlerAdapter，它用于对网络事件进行读写操作，通常我们只需要关注 channelRead 和 exceptionCaught 方法。下面对这两个方法进行简单说明。
 *
 * 第 17 行做类型转换，将 msg 转换成 Netty 的 ByteBuf 对象。ByteBuf 类似于 JDK 中的 java. Nio. ByteBuffer 对象，不过它提供了更加强大和灵活的功能。通过 ByteBuf 的 readableBytes 方法可以获取缓冲区可读的字节数，根据可读的字节数创建 byte 数组，通过 ByteBuf 的 readBytes 方法将缓冲区中的字节数组复制到新建的 byte 数组中，最后通过 new String 构造函数获取请求消息。这时对请求消息进行判断，如果是“QUERY TIME ORDER“则创建应答消息，通过 ChannelHandlerContext 的 write 方法异步发送应答消息给客户端。
 *
 * 第 30 行我们发现还
 *
 * 第 35 行，当发生异常时，关闭 ChannelHandlerContext，释放和 ChannelHandlerContext 相关联的句柄等资源。
 */
public class TimeServerHandler extends ChannelHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //将 msg 转换成 Netty 的 ByteBuf 对象。ByteBuf 类似于 JDK 中的 java. Nio. ByteBuffer 对象，不过它提供了更加强大和灵活的功能。
        ByteBuf buf = (ByteBuf) msg;
        //通过 ByteBuf 的 readableBytes 方法可以获取缓冲区可读的字节数，根据可读的字节数创建 byte 数组，通过 ByteBuf 的 readBytes 方法将缓冲区中的字节数组复制到新建的 byte 数组中.
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        //最后通过 new String 构造函数获取请求消息。这时对请求消息进行判断，如果是“QUERY TIME ORDER“则创建应答消息，通过 ChannelHandlerContext 的 write 方法异步发送应答消息给客户端。
        String body = new String(req, "UTF-8");
        System.out.println("The time server receive order : " + body);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.write(resp);
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        /**
         * 调用了 ChannelHandlerContext 的 flush 方法，它的作用是将消息发送队列中的消息写入到 SocketChannel 中发送给对方。
         * 从性能角度考虑，为了防止频繁地唤醒 Selector 进行消息发送，Netty 的 write 方法并不直接将消息写入 SocketChannel 中，
         * 调用 write 方法只是把待发送的消息放到发送缓冲数组中，
         * 再通过调用 flush 方法，将发送缓冲区中的消息全部写到 SocketChannel 中。
         */
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}