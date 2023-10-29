package com.murong.rpc.config;

import com.murong.rpc.server.RpcServer;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncRunner implements ApplicationRunner {
    Logger logger = LoggerFactory.getLogger(AsyncRunner.class);
    @Autowired
    NodeConfig nodeConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        rpcStart();
    }

    /**
     * 开启本机服务
     *
     * @throws Exception
     */
    @Async
    public void rpcStart() throws Exception {
        logger.info("开始启动节点服务...");
        RpcServer rpcServer = new RpcServer(nodeConfig.getNodePort(), new NioEventLoopGroup(), new NioEventLoopGroup());
        rpcServer.start();
        logger.info("结束启动节点服务...");
    }
}
