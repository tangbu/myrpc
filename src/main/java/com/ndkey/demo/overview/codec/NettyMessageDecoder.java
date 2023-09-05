package com.ndkey.demo.overview.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndkey.demo.overview.struct.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;

/**
 * @author tangbu
 */
public class NettyMessageDecoder extends ChannelInboundHandlerAdapter {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        ByteBuf byteBuf= (ByteBuf) obj;
        if (byteBuf == null){
            return ;
        }

        NettyMessage message = new NettyMessage();
        message.setVersion(byteBuf.readByte());
        message.setLength(byteBuf.readInt());
        message.setType(byteBuf.readByte());
        message.setPriority(byteBuf.readByte());


        byte[] bodyBytes = new byte[message.getLength()-7];
        byteBuf.readBytes(bodyBytes);
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readValue(bodyBytes, JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        message.setBody(jsonNode);
        ctx.fireChannelRead(message);
    }


}
