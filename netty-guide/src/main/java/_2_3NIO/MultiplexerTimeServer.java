package _2_3NIO;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * 用于处理多个客户端的并发接入。复制轮询多路复用器 Selector.
 */

public class MultiplexerTimeServer implements Runnable {

    private Selector selector;
    private ServerSocketChannel servChannel;
    private volatile boolean stop;



    /**
     * 初始化多路复用器、绑定监听端口
     *
     * 行为构造方法，在构造方法中进行资源初始化，创建多路复用器 Selector、ServerSocketChannel，
     * 对 Channel 和 TCP 参数进行配置。例如，将 ServerSocketChannel 设置为异步非阻塞模式，它的 backlog 设置为 1024。
     * 系统资源初始化成功后，将 ServerSocketChannel 注册到 Selector，监听 SelectionKey. OP_ ACCEPT 操作位；
     * 如果资源初始化失败（例如端口被占用），则退出。
     *
     * @param port
     */
    public MultiplexerTimeServer(int port) {
        try {
            selector = Selector.open();
            servChannel = ServerSocketChannel.open();
            servChannel.configureBlocking(false);  // 设置为异步阻塞模式
            servChannel.socket().bind(new InetSocketAddress(port), 1024);// 设置 backlog =1024  ， requested maximum length of the queue of incoming connections.
            servChannel.register(selector, SelectionKey.OP_ACCEPT); // 设置 操作位为 ACCEPT
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void stop() {
        this.stop = true;
    }



    @Override
    public void run() {
        /**
         * 它的休眠时间为 1 s，无论是否有读写等事件发生，selector 每隔 1 s 都被唤醒-次，selector 也提供了一个无参的 select 方法。
         * 当有处于就绪状态的 Channel 时，selector 将返回就绪状态的 Channel 的 SelectionKey 集合，
         * 通过对就绪状态的 Channel 集合进行迭代，可以进行网络的异步读写操作。
         */
        while (!stop) {
            try {
                //select()方法返回的int值表示有多少通道已经就绪。
                selector.select(1000);
                //一旦调用了select()方法，并且返回值表明有一个或更多个通道就绪了，然后可以通过调用selector的selectedKeys()方法，
                // 访问“已选择键集（selected key set）”中的就绪通道。
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            /**
             * 行处理新接入的客户端请求消息，
             * 根据 SelectionKey 的操作位进行判断即可获知网络事件的类型，
             * 通过 ServerSocketChannel 的 accept 接收客户端的连接请求并创建 SocketChannel 实例，
             * 完成上述操作后，相当于完成了 TCP 的三次握手，TCP 物理链路正式建立。
             * 注意，我们需要将新创建的 SocketChannel 设置为异步非阻塞，同时也可以对其 TCP 参数进行设置，例如 TCP 接收和发送缓冲区的大小等，作为入门的例子，例程没有进行额外的参数设置。
             */
            if (key.isAcceptable()) {
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();    // 类型为  ACCEPT 建立 连接（相当于TCP 3 次握手），
                sc.configureBlocking(false);

                sc.register(selector, SelectionKey.OP_READ); // add the new connection to the selector

            }

            /**
             * 行用于读取客户端的请求消息，首先创建一个 ByteBuffer，
             * 由于我们事先无法得知客户端发送的码流大小，作为例程，我们开辟一个 1 K 的缓冲区。
             * 然后调用 SocketChannel 的 read 方法读取请求码流。
             * 注意，由于我们已经将 SocketChannel 设置为异步非阻塞模式，因此它的 read 是非阻塞的。
             * 使用返回值进行判断，看读取到的字节数，返回值有以下三种可能的结果。
             * 返回值大于 0: 读到了字节，对字节进行编解码；
             * 返回值等于 0: 没有读取到字节，属于正常场景，忽略；
             * 返回值为-1: 链路已经关闭，需要关闭 SocketChannel，释放资源。
             * 当读取到码流以后，我们进行解码，
             * 首先对 readBuffer 进行 flip 操作，它的作用是将缓冲区当前的 limit 设置为 position, position 设置为 0，
             * 用于后续对缓冲区的读取操作。然后根据缓冲区可读的字节个数创建字节数组，调用 ByteBuffer 的 get 操作将缓冲区可读的字节数组复制到新创建的字节数组中，
             * 最后调用字符串的构造函数创建请求消息体并打印。如果请求指令是“QUERY TIME ORDER“则把服务器的当前时间编码后返回给客户端，
             * 下面我们看看异步发送应答消息给客户端的情况。
             */
            if (key.isReadable()) {
                // read the data
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024); // 1MB 的缓冲区
                int readBytes = sc.read(readBuffer); // 读取请求流
                if (readBytes > 0) {
                    readBuffer.flip();//将缓冲区当前的 limit 设置为 position , position 设置为 0 ， 用于后续对缓冲区的读取操作。

                    byte[] bytes = new byte[readBuffer.remaining()];  // 根据缓冲区可读的数组复制到新创建的字节数组中
                    readBuffer.get(bytes);

                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                    doWrite(sc, currentTime);
                } else if (readBytes < 0) {
                    // 对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    //  读到 0 字节， 忽略
                }

            }
        }
    }

    /**
     * 将应答消息异步发送给客户端。我们看下关键代码；
     * 首先将字符串编码成字节数组，根据字节数组的容量创建 ByteBuffer，调用 ByteBuffer 的 put 操作将字节
     * 数组复制到缓冲区中，然后对缓冲区进行 flip 操作，
     * 最后调用 SocketChannel 的 write 方法将缓冲区中的字节数组发送出去。
     * 需要指出的是，由于 SocketChannel 是异步非阻塞的，它并不保证一次能够把需要发送的字节数组发送完，
     * 此时会出现“写半包”问题，我们需要注册写操作，不断轮询 Selector 将没有发送完的 ByteBuffer 发送完毕，可以通过 ByteBuffer 的 hasRemaining（）方法判断消息是否发送完成。
     * 此处仅仅是个简单的入门级例程，没有演示如何处理“写半包”场景，后续的章节会有详细说明。
     * @param sc
     * @param response
     * @throws IOException
     */
    private void doWrite(SocketChannel sc, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();

            sc.write(writeBuffer);
        }
    }



}