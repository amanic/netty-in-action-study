package myOwnTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketConventionServer {

    public static void main(String[] args) throws Exception {
        doService();
    }

    public static void doService() throws IOException {
        int port = 55533;
        final ServerSocket server = new ServerSocket(port);
        System.out.println("server将一直等待连接的到来");
        final Socket socket = server.accept();
        final InputStream inputStream = socket.getInputStream();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        // 监听指定的端口
                        // server将一直等待连接的到来
                        // 建立好连接后，从socket中获取输入流，并建立缓冲区进行读取
//                        BufferedReader read=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                        byte[] bytes = new byte[1024];
                        int len;
                        StringBuilder sb = new StringBuilder();
                        while ((len = inputStream.read(bytes)) != -1 && !sb.toString().contains("end")) {
                            //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
                            sb.append(new String(bytes, 0, len,"UTF-8"));
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
