package com.murong.rpc.config;

import com.murong.rpc.client.ClientSitePool;
import com.murong.rpc.server.RpcServer;
import com.murong.rpc.service.NodeService;
import com.murong.rpc.util.ThreadUtil;
import com.murong.rpc.util.TimeUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PoolManagerRunner implements ApplicationRunner {

    Logger logger = LoggerFactory.getLogger(PoolManagerRunner.class);

    @Autowired
    NodeConfig nodeConfig;

    @Autowired
    NodeService nodeService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 首先启动当前节点
        rpcStart();
        // 管理本机链接远程的端口
        ExecutorPool.getExecutorService().submit(() -> { // 每15s检测下连接
            TimeUtil.execDapByFunction(() -> {
                // 每10s检测下连接池
                logger.info("尝试清理无效的连接...");
                ThreadUtil.execSilentException(() -> ClientSitePool.monitorAndDestory(), e -> e.printStackTrace());
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 间隔3s后开始注册链接
                logger.info("尝试建立中心连接...");
                ThreadUtil.execSilentException(() -> nodeService.acceptCenter(), e -> e.printStackTrace());
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 同时向注册中心注册地址
                logger.info("注册本机节点地址...");
                ThreadUtil.execSilentException(() -> nodeService.registerNode(), e -> e.printStackTrace());
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 向注册中心同步目录
                logger.info("普通节点向中心节点获取配置信息...");
                ThreadUtil.execSilentException(() -> nodeService.syncConf(), e -> e.printStackTrace());
                // 中心节点更新节点信息
                TimeUtil.execDapByFunction(() -> false, 2000, 1);
                logger.info("中心节点间的节点配置信息同步...");
                ThreadUtil.execSilentException(() -> nodeService.syncCenterConf(), e -> e.printStackTrace());

                logger.info("初始化完毕...");
                return false;
            }, 10000);
        });
    }

    /**
     * 启动rpc服务
     *
     * @throws Exception
     */
    public boolean rpcStart() throws Exception {
        logger.info("开始启动节点服务...");
        RpcServer rpcServer = new RpcServer(nodeConfig.getNodePort(), new NioEventLoopGroup(), new NioEventLoopGroup());
        rpcServer.start();
        logger.info("结束启动节点服务...");
        Thread.sleep(3000);
        return true;
    }
}
