package ioLearning;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * {@link 张龙P41_P44 Sec1}
 * @auther chen.haitao
 * @date 2019-01-30
 *
 * 聊天室：客户端与服务端。
 */
public class SelectorLearning {

    private static Map<String,SocketChannel> clientMap = new HashMap<>();
    /**
     * 这里只是用一个通道，与oio不通，这里是一个线程。
     *
     * 服务端需要保存客户端的信息：连接
     * @param args
     */
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket socket = serverSocketChannel.socket();
        socket.bind(new InetSocketAddress(8899));


        Selector selector = Selector.open();

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("监听端口：" + 8899);

        for (;;){
            int i = selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectionKeys.iterator();
            while (it.hasNext()){
                SelectionKey key = it.next();
                if(key.isAcceptable()){
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();//这里能强制转换，是因为SelectionKey.OP_ACCEPT这个事件注册的通道，是ServerSocketChannel类型的。
                    SocketChannel socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ);//到目前为止，已经在selector上面注册了两个channel，但是两个channel的功能是不一样的
                    it.remove();
                    System.out.println("接收到连接："+socketChannel);
                    String mapKey = "【"+ UUID.randomUUID().toString() +"】";
                    clientMap.put(mapKey,socketChannel);
                }else if(key.isReadable()){
                    ByteBuffer buffer = ByteBuffer.allocate(512);
                    SocketChannel channel = (SocketChannel) key.channel();
                    int read = channel.read(buffer);
                    while (read>0){
                        buffer.flip();
                        Iterator<String> iterator = clientMap.keySet().iterator();
                        Charset charset = Charset.forName("utf-8");
                        String recievedMsg = String.valueOf(charset.decode(buffer).array());
                        System.out.println(channel+"："+recievedMsg);
                        while (iterator.hasNext()){
                            SocketChannel channel1 = clientMap.get(iterator.next());
                            if(channel==channel1){
                                channel1.write(ByteBuffer.wrap(("【自己】："+recievedMsg).getBytes()));
                                System.out.println("自己");
                            }else {
                                channel1.write(ByteBuffer.wrap(("【"+channel.getRemoteAddress()+"】："+recievedMsg).getBytes()));
                                System.out.println("别人");
                            }
                        }
                        buffer.clear();
                        read = channel.read(buffer);
                    }
                    it.remove();
                }

            }
        }
    }
}
