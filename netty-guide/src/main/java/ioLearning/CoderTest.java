package ioLearning;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 *
 * 编解码问题
 * @auther chen.haitao
 * @date 2019-01-30
 */

public class CoderTest {

    public static void main(String[] args) throws Exception {
        String inFile = "input.txt";
        String outFile = "output.txt";

        RandomAccessFile file1 = new RandomAccessFile(inFile, "r");
        RandomAccessFile file2 = new RandomAccessFile(outFile, "rw");

        long length = new File(inFile).length();

        FileChannel channel1 = file1.getChannel();
        FileChannel channel2 = file2.getChannel();


        MappedByteBuffer input = channel1.map(FileChannel.MapMode.READ_ONLY, 0, length);
//        MappedByteBuffer output = channel2.map(FileChannel.MapMode.READ_WRITE, 0, length);

        Charset u8 = Charset.forName("utf-8");
        Charset ladin = Charset.forName("iso-8859-1");
        CharsetDecoder charsetDecoder = u8.newDecoder();


        CharsetEncoder charsetEncoder = ladin.newEncoder();

        CharBuffer decode = charsetDecoder.decode(input);

        ByteBuffer encode = charsetEncoder.encode(decode);

        channel2.write(encode);

        file1.close();
        file2.close();






    }
}