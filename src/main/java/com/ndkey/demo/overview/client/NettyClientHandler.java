package com.ndkey.demo.overview.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndkey.demo.overview.future.RpcReqResponseFuture;
import com.ndkey.demo.overview.pojo.RpcRequest;
import com.ndkey.demo.overview.pojo.RpcResponse;
import com.ndkey.demo.overview.struct.NettyMessage;
import com.ndkey.demo.overview.struct.Header;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author tangbu
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private ObjectMapper mapper = new ObjectMapper();

    private ChannelHandlerContext ctx;

    private Map<String, RpcReqResponseFuture> reqRespFutures = new HashMap<>();

    private Executor executor = Executors.newFixedThreadPool(5);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接服务提供者" + ctx.channel().remoteAddress() + "成功");
        this.ctx = ctx;
        ChannelHandlerManager.register(this);
    }

    public RpcReqResponseFuture sendRpcRequest(RpcRequest request) throws JsonProcessingException {
        RpcReqResponseFuture future = new RpcReqResponseFuture(request, executor);
        reqRespFutures.put(request.getRequestId(), future);

        NettyMessage nettyMessage = new NettyMessage();
        Header header = new Header();
        header.setType((byte) 1);
        header.setPriority((byte) 2);
        nettyMessage.setHeader(header);
        nettyMessage.setBody(mapper.readValue(mapper.writeValueAsString(request), JsonNode.class));

        ctx.channel().writeAndFlush(nettyMessage);
        return future;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        System.out.println("服务器回复的Frame:" + message);
        JsonNode body = message.getBody();
        RpcResponse response = mapper.readValue(body.toString(), RpcResponse.class);
        String requestId = response.getRequestId();
        RpcReqResponseFuture future = reqRespFutures.get(requestId);
        future.done(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
