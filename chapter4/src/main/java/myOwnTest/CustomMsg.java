package myOwnTest;

/**
 * 1）type表示发送端的系统类型
 *
 * 2）flag表示发送信息的类型，是业务数据，还是心跳包数据
 *
 * 3）length表示主题body的长度
 *
 * 4）body表示主题信息
 */
public class CustomMsg {

    //类型  系统编号 0xAB 表示A系统，0xBC 表示B系统
    private byte type;

    //信息标志  0xAB 表示心跳包    0xBC 表示超时包  0xCD 业务信息包
    private byte flag;

    //主题信息的长度
    private int length;

    //主题信息
    private String body;

    public CustomMsg() {

    }

    public CustomMsg(byte type, byte flag, int length, String body) {
        this.type = type;
        this.flag = flag;
        this.length = length;
        this.body = body;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
