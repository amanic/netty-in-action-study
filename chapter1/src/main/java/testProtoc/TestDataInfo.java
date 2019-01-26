package testProtoc;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Arrays;

/**
 * @auther chen.haitao
 * @date 2019-01-26
 */
public class TestDataInfo {

    public static void main(String[] args) throws InvalidProtocolBufferException {

        //先将对象构造出来
        DataInfo.Student student = DataInfo.Student.newBuilder()
                .setAddress("地址")
                .setName("名字")
                .setAge(1)
                .build();

        //转化为字节数据,可用于在网络上进行传输
        byte[] byteArray = student.toByteArray();

        for (byte b : byteArray){
            System.out.print(b);
        }
        System.out.println();
        DataInfo.Student parseFrom = DataInfo.Student.parseFrom(byteArray);



        System.out.println(parseFrom);


    }
}
