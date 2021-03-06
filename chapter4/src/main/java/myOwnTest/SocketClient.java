package myOwnTest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {
    public static void main(String args[]) throws Exception {
        // 要连接的服务端IP地址和端口
        String host = "127.0.0.1";
        int port = 55533;
        // 与服务端建立连接
        try {
            Scanner sc = new Scanner(System.in);
            while(sc.hasNext()){
                String message=sc.nextLine();
                if(message.equals("bye")){
                    break;
                }
                Socket socket = new Socket(host, port);
                // 建立连接后获得输出流
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(message.getBytes("UTF-8"));
                //这里会调用socket的close方法，并且本次socket关闭了之后才意味着一次会话结束，本例对应的SocketServer才会对本次会话进行相应的反应
                outputStream.close();
            }
            Socket socket = new Socket(host, port);
            // 建立连接后获得输出流
            OutputStream outputStream = socket.getOutputStream();
            socket.getOutputStream().write("bye".getBytes("UTF-8"));
            outputStream.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

}
