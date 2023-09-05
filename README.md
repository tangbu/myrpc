## 概要
RPC（Remote Procedure Call）是指远程过程调用，也就是说两台服务器A，B，一个应用部署在A服务器上，想要调用B服务器上应用提供的函数/方法。在分布式系统中的系统环境建设和应用程序设计中有着广泛的应用。

常见的RPC框架有
* Apache Dubbo
* Google gRPC
* Apache Thrift
* Spring Cloud的Http实现

优秀的开源框架有高性能，可以像调用本地方法一样调用远程服务，本文着重讨论以下流程的实现
* 低侵入
* 利用Netty自定义网络协议完成远程调用

本文的代码可在github上自取，
链接：https://github.com/tangbu/myrpc

## RPC的流程
![image.png](/img/bVc9xUM)
以上，我们可以看到在实现RPC的过程中，我们需要着重处理一下几点
1. 低侵入(我们使用动态代理来实现方法级别直接调用)
2. 实现RpcRequest和RpcResponse的序列化和反序列化
3. 基于TCP自定义报文，承载RpcRequest和RpcResponse
4. 处理网络连接，网络传输

## 代码实现
### 动态代理实现低侵入(我们使用jdk动态代理)
假设应用层存在这样一个接口
```
public interface HelloWorldService {

    String helloWorld(String name);

}
```
我们在调用HelloWorldService#helloWorld的时候，希望自定义里面的逻辑，使用RPC来调用，为此我们就使用动态代理来实现
```
public class DynamicProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws ExecutionException, InterruptedException, JsonProcessingException {
       
        System.out.println("在调用方法时走到了动态代理里面");
        return null;
    }
}
```
在真正调用helloWorld方法的时候使用如下代码
```
        Class<?> helloWorldServiceClass = HelloWorldService.class;

        //创建代理类对象
        HelloWorldService so = (HelloWorldService) Proxy.newProxyInstance(helloWorldServiceClass.getClassLoader(),
                new Class[]{HelloWorldService.class}, new DynamicProxy());
        String result1 = so.helloWorld("zhangsan");
```
此时，原有接口的逻辑就调到了动态代理方法里面。之后，我们会将RPC的实现封装在DynamicProxy这个方法里的实现中。

### 封装RpcRequest对象和RpcResponse对象
在执行远程调用的时候，必须告诉远程服务，我需要调用那个类，那个方法，方法参数是哪些，入参是什么才可以让他返回结果给我，所以需要封装一下RpcRequest对象和RpcResponse对象
```
public class RpcRequest {

    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private int version;
// getter setter...
}

public class RpcResponse  {

    private String requestId;
    private boolean success;
    private String message;
    private Object result;
// getter setter...
}
```
### 定义网络协议，将RpcRequest和RpcResponse写成字节放在网络报文中传输
#### 自定义报文结构
```
0----7----15---23---31
|  1 |       2      |    
---------------------
| 2  | 3  |  4 |.....        4后面的是消息体  
---------------------
......5.......
--------------------- 

序号1 0-7 version  1byte
序号2 7-39 总报文长度 4byte
序号3 39-47 type消息类型 1byte
序号4 47-77 priority消息优先级 1byte
序号5 根据报文总长度减掉1-4的长度就是5的长度
```
相应的根据这个报文结构，可以抽象出我们的TCP的报文Java类
```
/**
 * @author tangbu
 */
public final class NettyMessage {

    private byte version = 1;
    private int length;// 消息长度
    private byte type;// 消息类型
    private byte priority;// 消息优先级;
    private JsonNode body; // 目前全部用json传递请求
}
```
#### 针对报文结构编写Netty的编码器和解码器
**编码器的实现** 由NettyMessage对象变成网络字节
```
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
        // 最后填充报文长度
        out.setInt(1, out.readableBytes());
    }
}

```
**解码器的实现**由网络字节变成NettyMessage对象

网络报文接收端需要做两件事情
1. 根据报文的Length字段的长度从TCP流中读取一整个NettyMessage对象长度。
   发送到网络中的字节以流的形式传输，如果没有指定的拆包规则，报文就像没有标点符号一样字节发送到接收端，造成上层应用无法识别，所以需要拆包，netty提供了针对固定报文结构的拆包器，对于我们的报文来说，长度占4个字节、报文首部偏移量为1，所以使用这个
```
new LengthFieldBasedFrameDecoder(1460, 1, 4, -5, 0)
```
这个来进行拆包, 经过这个解码器的报文就被拆成一整个NettyMessage的一段段字节了

2. 读取到字节需要反序列化成一个NettyMessage对象。
   相应的字节需要转换成NettyMessage对象，我们就使用一个对象解码器来进行解码
```
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


        byte[] bodyBytes = new byte[message.getLength() - 7];
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
```
这样，网络请求接收端就可以把接受到的字节读取成NettyMessage对象了。
### 处理RPC的客户端和服务端逻辑
#### RPC客户端的请求
处理逻辑封装在动态代理类中
```
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
```
与此同时，在客户端记录连接，和request编号和返回的response对应形成回调。
```
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
        nettyMessage.setType((byte) 1);
        nettyMessage.setPriority((byte) 2);
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
```

在服务端注册好真正的HelloWorld实现类来执行结果，返回RpcResponse
```
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
```
## 测试
### 启动两个NettyServer，
绑定8888和8889，分别注册HelloWorldImpl1和HelloWorldImpl2的实现
```
public class NettyServer1 {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        Map<String, Object> serviceMap = new HashMap<>();
// HelloWorldServiceImpl1
        serviceMap.put(HelloWorldService.class.getName(), new HelloWorldServiceImpl1());
        NettyServerHandler.serviceMap = serviceMap;

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1460, 1, 4, -5, 0));
                            ch.pipeline().addLast(new NettyMessageDecoder());
                            ch.pipeline().addLast(new NettyMessageEncoder());
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });
// 一个服务绑定8888，一个绑定8889端口 
            ChannelFuture cf = bootstrap.bind(8888).sync();
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {

                    if (channelFuture.isSuccess()) {
                        System.out.println("监听端口 " + 8888 + " 成功");
                    } else {
                        System.out.println("监听端口 " + 8888 + " 失败");
                    }


                }
            });

            cf.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}
```
### 构建Netty客户端，测试时同时和两个NettyServer建立连接
```
public class NettyClient implements Runnable {

    private String ip;
    private int port;

    public NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup(1);

        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1460, 1, 4, -5, 0));
                            ch.pipeline().addLast(new NettyMessageDecoder());
                            ch.pipeline().addLast(new NettyMessageEncoder());
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            System.out.println("客户端 ok..");
            ChannelFuture connect = bootstrap.connect(new InetSocketAddress(ip, port));
            try {
                connect.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            group.shutdownGracefully();
        }

    }
}
```
### 执行程序
```
@Test
    public void test2() throws InterruptedException {
        new Thread(new NettyClient("127.0.0.1",8888)).start();
        new Thread(new NettyClient("127.0.0.1",8889)).start();

        Thread.sleep(3000);
        
        Class<?> helloWorldServiceClass = HelloWorldService.class;

        System.out.println();
        System.out.println();

        //创建代理类对象
        HelloWorldService so = (HelloWorldService) Proxy.newProxyInstance(helloWorldServiceClass.getClassLoader(),
                new Class[]{HelloWorldService.class}, new DynamicProxy());
        String result1 = so.helloWorld("zhangsan");
        
        Thread.sleep(1000000L);
    }
```
![image.png](/img/bVc9yyE)
如图，远程调用成功，简易的RPC代码得到了实现
## 展望
后续可以改进的方案
1. 增加Netty的异步实现，减少收发请求的阻塞
2. 结合spring在BeanPostProcessor中对Bean进行增强，统一动态代理。
3. 可以单独抽出RpcClient部分和RpcServer部分，不对应用暴露细节
4. 增加注册中心，容错，负载均衡，实现高可用
