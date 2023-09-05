package com.ndkey.demo.overview.server;


import com.ndkey.demo.overview.codec.NettyMessageDecoder;
import com.ndkey.demo.overview.codec.NettyMessageEncoder;
import com.ndkey.demo.overview.service.HelloWorldService;
import com.ndkey.demo.overview.service.HelloWorldServiceImpl2;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tangbu
 */
public class NettyServer2 {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);


        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(HelloWorldService.class.getName(), new HelloWorldServiceImpl2());
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

            ChannelFuture cf = bootstrap.bind(8889).sync();
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {

                    if (channelFuture.isSuccess()) {
                        System.out.println("监听端口 "+8889+" 成功");
                    } else {
                        System.out.println("监听端口 "+8889+" 失败");
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
