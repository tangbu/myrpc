package com.ndkey.demo.overview.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ndkey.demo.overview.client.ChannelHandlerManager;
import com.ndkey.demo.overview.client.NettyClientHandler;
import com.ndkey.demo.overview.future.RpcReqResponseFuture;
import com.ndkey.demo.overview.pojo.RpcRequest;
import com.ndkey.demo.overview.pojo.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author tangbu
 */
public class DynamicProxy implements InvocationHandler {


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws ExecutionException, InterruptedException, JsonProcessingException {
        if ("toString".equals(method.getName())){
            return proxy.toString();
        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(1);

        System.out.println("动态代理封装的request"+ request);

        System.out.println("----------------执行远程调用--------");

        RpcResponse response = invokeRpc(request);

        System.out.println("远程调用返回的结果"+ response);

        return response.getResult();
    }


    int count = 0;
    private RpcResponse invokeRpc(RpcRequest request) throws ExecutionException, InterruptedException, JsonProcessingException {
        count++;
        NettyClientHandler nettyClientHandler = ChannelHandlerManager.chooseHandler("127.0.0.1", count % 2 == 0 ? 8888 : 8889);
        RpcReqResponseFuture rpcReqResponseFuture = nettyClientHandler.sendRpcRequest(request);

        return rpcReqResponseFuture.get();
    }
}
