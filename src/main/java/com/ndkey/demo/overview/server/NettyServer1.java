package com.ndkey.demo.overview.server;


import com.ndkey.demo.overview.codec.NettyMessageDecoder;
import com.ndkey.demo.overview.codec.NettyMessageEncoder;
import com.ndkey.demo.overview.service.HelloWorldService;
import com.ndkey.demo.overview.service.HelloWorldServiceImpl1;
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
public class NettyServer1 {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        Map<String, Object> serviceMap = new HashMap<>();
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
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1460, 4, 4, -8, 0));
                            ch.pipeline().addLast(new NettyMessageDecoder());
                            ch.pipeline().addLast(new NettyMessageEncoder());
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });

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
