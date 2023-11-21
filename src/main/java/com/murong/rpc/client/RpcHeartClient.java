package com.murong.rpc.client;

import com.murong.rpc.client.handler.RpcClientHeartHandler;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


public class RpcHeartClient extends RpcDefaultClient {

    public RpcHeartClient(String host, int port, NioEventLoopGroup nioEventLoopGroup) {
        super(host, port, nioEventLoopGroup);
        this.channel.pipeline()
                .addLast(new IdleStateHandler(30, 5, 0, TimeUnit.SECONDS))
                .addLast(new RpcClientHeartHandler(this));
    }

    public RpcHeartClient(String host, int port) {
        this(host, port, new NioEventLoopGroup());
    }

}
