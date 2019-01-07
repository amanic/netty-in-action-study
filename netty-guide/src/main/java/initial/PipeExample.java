package initial;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * 测试线程之间的通信：
 * Java IO中的管道为运行在同一个JVM中的两个线程提供了通信的能力。所以管道也可以作为数据源以及目标媒介。
 *
 * 你不能利用管道与不同的JVM中的线程通信(不同的进程)。在概念上，Java的管道不同于Unix/Linux系统中的管道。在Unix/Linux中，运行在不同地址空间的两个进程可以通过管道通信。在Java中，通信的双方应该是运行在同一进程中的不同线程。
 *
 *
 * 通过Java IO创建管道
 * 可以通过Java IO中的PipedOutputStream和PipedInputStream创建管道。一个PipedInputStream流应该和一个PipedOutputStream流相关联。一个线程通过PipedOutputStream写入的数据可以被另一个线程通过相关联的PipedInputStream读取出来。
 */
public class PipeExample {

    public static void main(String[] args) throws IOException, InterruptedException {

        final PipedOutputStream outputStream = new PipedOutputStream();
        final PipedInputStream inputStream = new PipedInputStream(outputStream);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write("jbv\r\njbvusb粑粑波比".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

//        Thread.sleep(2000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int data;
                while (true){
                    try {
                        byte[] bytes = new byte[1024];
                        if ((data = inputStream.read(bytes))!=-1) {
                            for (byte b : bytes){
                                System.out.println((char)b);
                            }
                        }else
                            break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }).start();
    }
}
