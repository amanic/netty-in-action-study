package ioLearning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@link 张龙P41_P44 Sec2}
 *
 * @auther chen.haitao
 * @date 2019-01-30
 */
public class SelectorLearningClient1 {

    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            Selector selector = Selector.open();

            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8899));

            for (; ; ) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isConnectable()) {
                        final SocketChannel channel = (SocketChannel) selectionKey.channel();

                        if (channel.isConnectionPending()) {//判断是不是正在连接过程当中，需要主动触发连接操作。
                            channel.finishConnect();
                        }

                        final ByteBuffer buffer = ByteBuffer.wrap("建立连接".getBytes());
                        channel.write(buffer);

                        ExecutorService service = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
                        service.submit(new Runnable() {
                            @Override
                            public void run() {
                                while (true) {
                                    try {
                                        buffer.clear();
                                        InputStreamReader reader = new InputStreamReader(System.in);
                                        BufferedReader bufferedReader = new BufferedReader(reader);
                                        buffer.put(bufferedReader.readLine().getBytes());
                                        buffer.flip();
                                        channel.write(buffer);
                                    } catch (Exception e) {
                                        break;
                                    }
                                }
                            }
                        });
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        iterator.remove();
                    } else if (selectionKey.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(512);
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        int read = channel.read(buffer);
                        while (read > 0) {
                            String recievedMsg = new String(buffer.array(), 0, read);
                            System.out.println(channel + "：" + recievedMsg);
                            buffer.clear();
                            read = channel.read(buffer);
                        }
                        iterator.remove();
                    }

                }

            }


        } catch (Exception e) {

        }
    }
}
