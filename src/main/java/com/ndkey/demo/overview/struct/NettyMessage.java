package com.ndkey.demo.overview.struct;


import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author tangbu
 */
public final class NettyMessage {

    private byte version = 1;
    private int length;// 消息长度
    private byte type;// 消息类型
    private byte priority;// 消息优先级;
    private JsonNode body; // 目前全部用json传递请求

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
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

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NettyMessage{" +
                "version=" + version +
                ", length=" + length +
                ", type=" + type +
                ", priority=" + priority +
                ", body=" + body +
                '}';
    }
}
