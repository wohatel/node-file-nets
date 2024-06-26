package com.murong.nets.server;

import com.murong.nets.initializer.StringChannelInitializer;
import com.murong.nets.interaction.RpcGc;
import com.murong.nets.server.handler.RpcMessageServerInteractionHandler;
import com.murong.nets.server.handler.RpcServerRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * 绑定服务器到监听的端口，配置Channel，将入站消息通知给EchoServerHandler实例
 *
 * @version 1.0.0
 * @since 2021/5/25上午12:55
 */
public class RpcServer {

    private Channel channel;
    private final int port;
    private final EventLoopGroup group;
    private final EventLoopGroup childGroup;

    public RpcServer(int port, EventLoopGroup group, EventLoopGroup childGroup) {
        this.port = port;
        this.group = group;
        this.childGroup = childGroup;
    }

    /**
     * 开启nettyServer
     */
    public void start() throws InterruptedException {
        StringChannelInitializer stringChannelInitializer = new StringChannelInitializer(new RpcMessageServerInteractionHandler(), new RpcServerRequestHandler());
        this.start(stringChannelInitializer);
    }

    /**
     * 开启nettyServer
     *
     */
    public void start(ChannelInitializer<SocketChannel> channelChannelInitializer) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(group, childGroup).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port)).childHandler(channelChannelInitializer);
        ChannelFuture future = b.bind().sync();
        this.channel = future.channel(); // 用于关闭server
        RpcGc.callWake(); // 唤醒gc处理请求数据
        future.channel().closeFuture().addListener(futureListener -> {
            if (futureListener.isSuccess()) {
                group.shutdownGracefully();
                childGroup.shutdownGracefully();
            }
        });
    }

    public void stop() {
        if (this.channel != null) {
            this.channel.close();
        }
    }

}
