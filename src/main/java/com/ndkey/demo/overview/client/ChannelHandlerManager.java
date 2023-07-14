package com.ndkey.demo.overview.client;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangbu
 */
public class ChannelHandlerManager {
    // id   handler
    private static Map<String, NettyClientHandler> handlers = new HashMap<>();

    public static void register(NettyClientHandler handler) {
        ChannelHandlerContext ctx = handler.getCtx();
        String host = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        int port = ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
        handlers.put(host + ":" + port, handler);
    }

    public static NettyClientHandler chooseHandler(String host, int port){
        return handlers.get(host + ":" + port);
    }
}
