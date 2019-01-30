package nia.chapter4;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * {@link 张龙P40 Sec1}
 * 代码清单 4-1 未使用 Netty 的阻塞网络编程
 * 上面的方式可以工作正常，但是这种阻塞模式在大连接数的情况就会有很严重的问题，
 * 如客户端连接超时，服务器响应严重延迟，性能无法扩展。
 * 为了解决这种情况，我们可以使用异步网络处理所有的并发连接，但问题在于 NIO 和 OIO 的 API 是完全不同的，
 * 所以一个用OIO开发的网络应用程序想要使用NIO重构代码几乎是重新开发。
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class PlainOioServer {
    public void serve(int port) throws IOException {
        //将服务器绑定到指定端口
        final ServerSocket socket = new ServerSocket(port);
        try {
            for(;;) {
                //接受连接
                final Socket clientSocket = socket.accept();
                System.out.println(
                        "Accepted connection from " + clientSocket);
                //创建一个新的线程来处理该连接，
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream out;
                        try {

//这里会阻塞                   byte[] bytes = new byte[10];
//                            clientSocket.getInputStream().read(bytes);
//                            System.out.println(bytes.length);

                            //将消息写给已连接的客户端
                            out = clientSocket.getOutputStream();
                            Thread.sleep(5000);
                            out.write("Hi!\r\n".getBytes(
                                    Charset.forName("UTF-8")));
                            out.flush();
                            //关闭连接
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException ex) {
                                // ignore on close
                            }
                        }
                //启动线程
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 此处运行之后使用telnet命令：telnet 127.0.0.1 8080
     * 打印如下：
     * Trying 127.0.0.1...
     * Connected to localhost.
     * Escape character is '^]'.
     * Hi!
     * Connection closed by foreign host.
     * 以上是作为客户端，服务端不关闭。
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new PlainOioServer().serve(8080);
    }
}
