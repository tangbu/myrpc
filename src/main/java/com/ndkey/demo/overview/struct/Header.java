package com.ndkey.demo.overview.struct;

/**
 * @author tangbu
 */
public class Header {

    private int version = 11111;

    private int length;// 消息长度

    private byte type;// 消息类型

    private byte priority;// 消息优先级


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }


    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Header{" +
                "version=" + version +
                ", length=" + length +
                ", type=" + type +
                ", priority=" + priority +
                '}';
    }
}
