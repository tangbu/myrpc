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
        nettyMessage.setLength(123);
        nettyMessage.setType((byte) 1);
        nettyMessage.setPriority((byte) 7);
        TextNode textNode = JsonNodeFactory.instance.textNode("i,am body");
        nettyMessage.setBody(textNode);
        return nettyMessage;
    }

    public ByteBuf encode(NettyMessage msg) throws Exception {
        ByteBuf out = Unpooled.buffer();
        out.writeByte(msg.getVersion());
        out.writeInt(msg.getLength());  // 后续需要进行修改
        out.writeByte(msg.getType());
        out.writeByte(msg.getPriority());

        JsonNode body = msg.getBody();
        encoder.encode(body, out);
        out.setInt(1, out.readableBytes());
        return out;
    }

    public NettyMessage decode(ByteBuf in) throws Exception {
        NettyMessage message = new NettyMessage();
        message.setVersion(in.readByte());
        message.setLength(in.readInt());
        message.setType(in.readByte());
        message.setPriority(in.readByte());

        JsonNode body = decoder.decode(in, message.getLength()-7);
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
