package nia.chapter4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 代码清单 4-2 未使用 Netty 的异步网络编程
 * 这里使用到了nio。
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class PlainNioServer {
    public static void serve(int port) throws IOException {
        //打开ServerSocketChannel,用于监听客户端的连接，他是所有客户端连接的父管道。
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocketChannel serverChannela = ServerSocketChannel.open();
        //设置为非阻塞模式
        serverChannel.configureBlocking(false);
        serverChannela.configureBlocking(false);
        //将服务器绑定到选定的端口
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannela.socket().bind(new InetSocketAddress(port+1));
        //打开Selector来处理 Channel
        Selector selector = Selector.open();
        //将ServerSocketChannel注册到Selector以接受连接
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverChannela.register(selector, SelectionKey.OP_ACCEPT);
        final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
        for (;;){
            try {
                //等待需要处理的新事件；阻塞将一直持续到下一个传入事件
                System.out.println("等待需要处理的新事件；阻塞将一直持续到下一个传入事件。");
                System.out.println("selector.select() = "+selector.select());
                System.out.println("有新事件，阻塞结束，开始执行业务操作。");
            } catch (IOException ex) {
                ex.printStackTrace();
                //handle exception
                break;
            }
            //获取所有接收事件的SelectionKey实例
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                i++;
                System.out.println("进入");
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    //检查事件是否是一个新的已经就绪可以被接受的连接
                    if (key.isAcceptable()) {
                        ServerSocketChannel server =
                                (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        //接受客户端，并将它注册到选择器
                        client.register(selector, SelectionKey.OP_WRITE |
                                SelectionKey.OP_READ, msg.duplicate());
                        System.out.println(
                                "Accepted connection from " + client);
                    }
                    //检查套接字是否已经准备好写数据
                    if (key.isWritable()) {
                        SocketChannel client =
                                (SocketChannel) key.channel();
                        ByteBuffer buffer =
                                (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            //将数据写到已连接的客户端
                            Thread.sleep(5000);
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        //关闭连接
                        client.close();
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                        // ignore on close
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("迭代结束，迭代次数 = "+ i);
        }
    }

    /**
     * 此处运行之后使用telnet命令：telnet 127.0.0.1 8080
     * 打印如下：
     * 等待需要处理的新事件；阻塞将一直持续到下一个传入事件。
     * selector.select() = 1
     * 有新事件，阻塞结束，开始执行业务操作。
     * 进入
     * Accepted connection from java.nio.channels.SocketChannel[connected local=/127.0.0.1:8080 remote=/127.0.0.1:61330]
     * 迭代结束，迭代次数 = 1
     * 等待需要处理的新事件；阻塞将一直持续到下一个传入事件。
     * selector.select() = 1
     * 有新事件，阻塞结束，开始执行业务操作。
     * 进入
     * 迭代结束，迭代次数 = 1
     * 等待需要处理的新事件；阻塞将一直持续到下一个传入事件。
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        serve(8080);
    }
}

