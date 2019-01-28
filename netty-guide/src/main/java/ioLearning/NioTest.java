package ioLearning;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;

/**
 * @auther chen.haitao
 * @date 2019-01-28
 */
public class NioTest {

    public static void main(String[] args) throws Exception {
        test4();
    }

    public static void test1() throws Exception {

        FileInputStream fileInputStream = new FileInputStream("IoTest.txt");
        FileChannel fileChannel = fileInputStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(512);
        System.out.println("总容量capacity = " + buffer.capacity() + ",limit" + buffer.limit() + ",position" + buffer.position());

        fileChannel.read(buffer);
        System.out.println("总容量capacity = " + buffer.capacity() + ",limit" + buffer.limit() + ",position" + buffer.position());

        buffer.flip();

        while (buffer.remaining() > 0) {
            System.out.println("总容量capacity = " + buffer.capacity() + ",limit" + buffer.limit() + ",position" + buffer.position());

            byte b = buffer.get();

            System.out.println("Character：" + (char) b);

            fileInputStream.close();
        }
    }

    public static void test2() throws Exception{

        FileOutputStream fileOutPutStream = new FileOutputStream("IoTest.txt");
        Channel outChannel = fileOutPutStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(512);

        byte[] buf = "hello world !".getBytes();
        for (int i = 0; i < buf.length; i++) {
            buffer.put(buf[i]);
            System.out.println("总容量capacity = " + buffer.capacity() + ",limit" + buffer.limit() + ",position" + buffer.position());
        }

        buffer.flip();
        System.out.println("总容量capacity = " + buffer.capacity() + ",limit" + buffer.limit() + ",position" + buffer.position());
        ((FileChannel) outChannel).write(buffer);
        fileOutPutStream.close();
    }

    /**
     * 测试，FileInputStream和FileOutputStream同时打开会清空文件内容？
     * @throws Exception
     */
    public static void test3() throws Exception{
        FileInputStream fileInPutStream = new FileInputStream("IoTest.txt");
        FileOutputStream fileOutPutStream = new FileOutputStream("IoTest.txt");

        fileInPutStream.close();
        fileOutPutStream.close();
    }


    /**
     * buffer.clear()特别重要
     * @throws Exception
     */
    public static void test4() throws Exception{
        FileInputStream fileInPutStream = new FileInputStream("input.txt");
        FileOutputStream fileOutPutStream = new FileOutputStream("output.txt");

        ByteBuffer buffer = ByteBuffer.allocate(128);

        FileChannel inCHannel = fileInPutStream.getChannel();
        FileChannel outChannel = fileOutPutStream.getChannel();


        while (true){
            buffer.clear();
            int read = inCHannel.read(buffer);
            if(-1 == read){
                break;
            }
            buffer.flip();
            outChannel.write(buffer);
        }

        fileInPutStream.close();
        fileOutPutStream.close();
    }






}