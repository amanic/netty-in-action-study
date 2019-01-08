package initial;


import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TestFileChannel {

    public static void main(String[] args) throws Exception {
        main1();
    }

    /**
     * 从文件中读取内容
     * @throws Exception
     */
    public static void main1() throws Exception {
        RandomAccessFile aFile = new RandomAccessFile("/Users/martea/Desktop/test.txt","rw");
        FileChannel channel = aFile.getChannel();
        //FileChannel实例的size()方法将返回该实例所关联文件的大小。如:
        System.out.println("文件channel大小 = "+channel.size());
        //可以使用FileChannel.truncate()方法截取一个文件。截取文件时，文件将中指定长度后面的部分将被删除。如：
        channel.truncate(77);
        ByteBuffer buf = ByteBuffer.allocate(10);
        int bytesRead = channel.read(buf);
        while (bytesRead!=-1){
            System.out.println(bytesRead);
            buf.flip();
            while (buf.hasRemaining()){
                System.out.println((char)buf.get());
            }
            buf.flip();
            buf.clear();
            bytesRead = channel.read(buf);
        }
    }

    /**
     * 向文件中写数据
     * @throws Exception
     */
    public static void main2() throws Exception{
        RandomAccessFile aFile = new RandomAccessFile("/Users/martea/Desktop/test.txt","rw");
        FileChannel channel = aFile.getChannel();
        String newData = "New String to write to file..." + System.currentTimeMillis();

        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());

        buf.flip();

        while(buf.hasRemaining()) {
            channel.write(buf);
        }
    }
}
