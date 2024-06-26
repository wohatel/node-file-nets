package com.murong.nets.client;

import com.murong.nets.client.handler.RpcClientHeartHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


public class RpcHeartClient extends RpcDefaultClient {

    private Long writeTime = System.currentTimeMillis();

    private boolean isReadOut;

    public RpcHeartClient(String host, int port, NioEventLoopGroup nioEventLoopGroup) {
        super(host, port, nioEventLoopGroup);
    }

    public RpcHeartClient(String host, int port) {
        this(host, port, new NioEventLoopGroup());
    }

    @Override
    public ChannelFuture connect() {
        ChannelFuture connect = super.connect();
        connect.channel().pipeline().addLast(new IdleStateHandler(30, 5, 0, TimeUnit.SECONDS)).addLast(new RpcClientHeartHandler(this));
        return connect;
    }

    public Long getWriteTime() {
        return writeTime;
    }

    public void setWriteTime(Long writeTime) {
        this.writeTime = writeTime;
    }

    public boolean isReadOut() {
        return isReadOut;
    }

    public void setReadOut(boolean readOut) {
        isReadOut = readOut;
    }

    public boolean isActive() {
        if (isReadOut) {
            return false;
        }
        long cha = System.currentTimeMillis() - writeTime;
        if (cha > 30 * 1000) {// 相隔这么久的时间没发送
            return false;
        }
        return true;
    }
}
