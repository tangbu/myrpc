package com.ndkey.demo.overview.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndkey.demo.overview.struct.NettyMessage;
import com.ndkey.exception.DkException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * @author tangbu
 */
public class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {
    private ObjectMapper mapper = new ObjectMapper();


    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf out) throws Exception {

        out.writeByte(msg.getVersion());
        out.writeInt(msg.getLength());
        out.writeByte(msg.getType());
        out.writeByte(msg.getPriority());

        JsonNode body = msg.getBody();
        try {
            byte[] jsonBytes = mapper.writeValueAsBytes(body);
            out.writeBytes(jsonBytes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        out.setInt(1, out.readableBytes());
    }
}
