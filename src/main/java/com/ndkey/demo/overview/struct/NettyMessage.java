package com.ndkey.demo.overview.struct;


import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author tangbu
 */
public final class NettyMessage {

    private Header header;
    private JsonNode body; // 基础类型 。。。 todo

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
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
                "header=" + header +
                ", body=" + body +
                '}';
    }
}
