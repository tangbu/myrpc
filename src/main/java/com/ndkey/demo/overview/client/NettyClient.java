package com.ndkey.demo.overview.client;

import com.ndkey.demo.overview.codec.NettyMessageDecoder;
import com.ndkey.demo.overview.codec.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;

/**
 * @author tangbu
 */
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

                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1460, 4, 4, -8, 0));
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
