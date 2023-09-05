package com.ndkey.demo.overview.server;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndkey.demo.overview.pojo.RpcRequest;
import com.ndkey.demo.overview.pojo.RpcResponse;
import com.ndkey.demo.overview.struct.NettyMessage;
import com.ndkey.exception.DkRuntimeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * @author tangbu
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    public static Map<String,Object> serviceMap = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        System.out.println("服务端收到的消息是:" + message);
        JsonNode rpcRequestBody = message.getBody();
        RpcRequest rpcRequest = mapper.readValue(rpcRequestBody.toString(), RpcRequest.class);
        RpcResponse rpcResponse = handleRpcRequest(rpcRequest);

        NettyMessage response = new NettyMessage();
        response.setType((byte) 1);
        response.setPriority((byte) 2);
        response.setBody(mapper.readValue(mapper.writeValueAsString(rpcResponse), JsonNode.class));

        ctx.channel().writeAndFlush(response);

    }

    private RpcResponse handleRpcRequest(RpcRequest rpcRequest) {
        String requestId = rpcRequest.getRequestId();
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        try {
            String className = rpcRequest.getClassName();
            String methodName = rpcRequest.getMethodName();
            Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
            Object[] parameters = rpcRequest.getParameters();

            Object o = serviceMap.get(className);
            if (o == null){
                throw new DkRuntimeException("服务不存在");
            }

            Class clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, parameterTypes);
            Object result = method.invoke(o, parameters);
            response.setSuccess(true);
            response.setResult(result);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }

        return response;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        ctx.fireExceptionCaught(cause);

    }
}
