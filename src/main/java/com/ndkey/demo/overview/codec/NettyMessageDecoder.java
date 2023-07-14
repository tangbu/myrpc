package com.ndkey.demo.overview.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.ndkey.demo.overview.struct.Header;
import com.ndkey.demo.overview.struct.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author tangbu
 */
public class NettyMessageDecoder extends ChannelInboundHandlerAdapter {
    private JsonObjectDecoder decoder = new JsonObjectDecoder();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        ByteBuf byteBuf= (ByteBuf) obj;
        if (byteBuf == null){
            return ;
        }

        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setVersion(byteBuf.readInt());
        header.setLength(byteBuf.readInt());
        header.setType(byteBuf.readByte());
        header.setPriority(byteBuf.readByte());

        JsonNode body = decoder.decode(byteBuf);
        message.setHeader(header);
        message.setBody(body);
        ctx.fireChannelRead(message);
    }


}
