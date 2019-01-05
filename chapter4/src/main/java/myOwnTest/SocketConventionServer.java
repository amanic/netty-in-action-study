package myOwnTest;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 在这里，约定好客户端结束消息以end结尾，所以客户端并不用没输入一次消息都再来一次socket断开与连接从而通知服务器端消息结束。
 *
 *
 * 客户端输入：
 *      你好呀，martea ！
 *      输入end
 *      我不好，amanic ！
 *      end
 * 服务端输出：
 *      server将一直等待连接的到来
 *      server成功建立连接，开始根据相应情况答应客户端发送的消息。
 *      get message from client: 你好呀，martea ！
 *      get message from client: 我不好，amanic ！
 *      。。。
 *      这里会根据客户端打印的消息打印出消息。约定好，如果客户端发送的消息以'end'结尾，则忽略并且打印这之前没有打印的消息。
 */
public class SocketConventionServer {

    public static void main(String[] args) throws Exception {
        doService();
    }

    public static void doService() throws IOException {
        int port = 55533;
        // 监听指定的端口
        final ServerSocket server = new ServerSocket(port);
        System.out.println("server将一直等待连接的到来");
        // server将一直等待连接的到来
        final Socket socket = server.accept();
        System.out.println("server成功建立连接，开始根据相应情况答应客户端发送的消息。");
        final InputStream inputStream = socket.getInputStream();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        byte[] bytes = new byte[1024];
                        int len;
                        StringBuilder sb = new StringBuilder();
                        len = inputStream.read(bytes);
                        while (len != -1) {
                            //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
                            String s = new String(bytes, 0, len, "UTF-8");
                            if(s.endsWith("end")){
                                break;
                            }
                            sb.append(s);
                            len = inputStream.read(bytes);
                        }
                        System.out.println("get message from client: " + sb);
                        if(sb.toString().equals("bye")){
                            System.out.println("bye!!!");
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println("出错了："+e);
                        break;
                    }
                }
                try {
                    server.close();
                } catch (IOException e) {
                    //TODO
                }
                System.out.println("程序结束！！！");
            }
        }).start();
    }
}
