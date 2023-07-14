/*
 * Copyright 2013-2018 Lilinfeng.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ndkey.demo.overview.codec;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ndkey.demo.overview.struct.Header;
import com.ndkey.demo.overview.struct.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 * @date 2014年3月15日
 */
public class TestCodeC {

    private JsonObjectEncoder encoder;
    private JsonObjectDecoder decoder;


    public TestCodeC() throws IOException {
        encoder = new JsonObjectEncoder();
        decoder = new JsonObjectDecoder();
    }

    public NettyMessage getMessage() {
        NettyMessage nettyMessage = new NettyMessage();
        Header header = new Header();
        header.setLength(123);
        header.setType((byte) 1);
        header.setPriority((byte) 7);
        nettyMessage.setHeader(header);
        TextNode textNode = JsonNodeFactory.instance.textNode("i,am body");
        nettyMessage.setBody(textNode);
        return nettyMessage;
    }

    public ByteBuf encode(NettyMessage msg) throws Exception {
        ByteBuf out = Unpooled.buffer();
        Header header = msg.getHeader();
        out.writeInt(header.getVersion());
        out.writeInt(header.getLength());  // 后续需要进行修改
        out.writeByte(header.getType());
        out.writeByte(header.getPriority());

        JsonNode body = msg.getBody();
        encoder.encode(body, out);
        out.setInt(4, out.readableBytes());
        return out;
    }

    public NettyMessage decode(ByteBuf in) throws Exception {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setVersion(in.readInt());
        header.setLength(in.readInt());
        header.setType(in.readByte());
        header.setPriority(in.readByte());

        JsonNode body = decoder.decode(in);
        message.setHeader(header);
        message.setBody(body);


        return message;
    }

    public static void test1() throws Exception {
        TestCodeC testC = new TestCodeC();
        NettyMessage message = testC.getMessage();
        System.out.println(message + "[body ] " + message.getBody());

        for (int i = 0; i < 5; i++) {
            ByteBuf buf = testC.encode(message);
            NettyMessage decodeMsg = testC.decode(buf);
            System.out.println(decodeMsg + "[body ] " + decodeMsg.getBody());
            System.out
                    .println("-------------------------------------------------");

        }
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        test1();
//        test2();

    }

}
