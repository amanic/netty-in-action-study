package myOwnTest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class SocketConventionClient {

    public static void main(String args[]) throws Exception {
        // 要连接的服务端IP地址和端口
        String host = "127.0.0.1";
        int port = 55533;
        // 与服务端建立连接
        try {
            Scanner sc = new Scanner(System.in);
            Socket socket = new Socket(host, port);
            OutputStream outputStream = socket.getOutputStream();
            while(sc.hasNext()){
                String message=sc.nextLine();
                if(message.equals("bye")){
                    break;
                }
                // 建立连接后获得输出流
                outputStream.write(message.getBytes("UTF-8"));
            }
            socket.getOutputStream().write("bye".getBytes("UTF-8"));
            outputStream.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
