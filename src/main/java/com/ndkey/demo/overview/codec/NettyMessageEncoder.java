package com.ndkey.demo.overview.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.ndkey.demo.overview.struct.Header;
import com.ndkey.demo.overview.struct.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * @author tangbu
 */
public class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {
    private JsonObjectEncoder encoder = new JsonObjectEncoder();
    public NettyMessageEncoder() {

    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf out) throws Exception {
        Header header = msg.getHeader();
        out.writeInt(header.getVersion());
        out.writeInt(header.getLength());  // 后续需要进行修改
        out.writeByte(header.getType());
        out.writeByte(header.getPriority());

        JsonNode body = msg.getBody();
        encoder.encode(body, out);
        out.setInt(4, out.readableBytes());

    }
}
