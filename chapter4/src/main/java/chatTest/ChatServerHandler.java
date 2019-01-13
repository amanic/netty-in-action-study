package chatTest;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {
    /**
     * A thread-safe Set Using ChannelGroup, you can categorize Channels into a
     * meaningful group. A closed Channel is automatically removed from the
     * collection,
     */
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    /**
     * 每当从服务端收到新的客户端连接时，客户端的 Channel 存入 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();

        // Broadcast a message to multiple Channels
        channels.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 加入\n");

        channels.add(ctx.channel());
//        System.out.println("完成channel的添加，当前channels = "+channels.size());
    }
    /**
     * 每当从服务端收到客户端断开时，客户端的 Channel 自动从 ChannelGroup 列表中移除了，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception { // (3)
        Channel incoming = ctx.channel();

        //将消息广播到多个Channel
        channels.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 离开\n");

        // 一个关闭的Channel将自动从ChannelGroup中移除，
        // so there is no need to do "channels.remove(ctx.channel());"
    }
    /**
     * 每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel。
     * 如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception { // (4)
        Channel incoming = ctx.channel();
        if(null==s||s.trim().equals("")||s.contains("爱情")){
            for (Channel channel : channels) {
                if(channel == incoming){
//                    System.out.println("channel == incoming");
                    channel.writeAndFlush("非法消息，发送失败。"+ "\n");
                    return;
                }
            }
        }
//        System.out.println("接收到消息，消息是："+s+"，channels的大小是："+channels.size());
        for (Channel channel : channels) {
            if (channel != incoming) {
//                System.out.println("channel != incoming");
                channel.writeAndFlush("[" + incoming.remoteAddress() + "]" + s + "\n");
            } else {
//                System.out.println("channel == incoming");
                channel.writeAndFlush("[自己]" + s + "\n");
            }
        }
    }
    /**
     * 服务端监听到客户端活动
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "在线");
    }
    /**
     * 服务端监听到客户端不活动
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "掉线");
    }
    /**
     * exceptionCaught() 事件处理方法是当出现 Throwable 对象才会被调用，
     * 即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
     * 在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉。
     * 然而这个方法的处理方式会在遇到不同异常的情况下有不同的实现，比如你可能想在关闭连接之前发送一个错误码的响应消息。
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
