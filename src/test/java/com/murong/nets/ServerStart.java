package com.murong.nets;


import com.murong.nets.server.RpcServer;
import io.netty.channel.nio.NioEventLoopGroup;

public class ServerStart {

    public static void start() throws Exception {
        RpcServer rpcServer = new RpcServer(8888, new NioEventLoopGroup(), new NioEventLoopGroup());
        rpcServer.start();
    }

}
