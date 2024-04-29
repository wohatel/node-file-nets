package com.murong.nets.config;

import com.murong.nets.client.ClientSitePool;
import com.murong.nets.server.RpcServer;
import com.murong.nets.service.NodeService;
import com.murong.nets.util.ThreadUtil;
import com.murong.nets.util.TimeUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Log
public class PoolManagerRunner implements ApplicationRunner {

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
                log.info("尝试清理无效的连接...");
                ThreadUtil.execSilentException(() -> ClientSitePool.monitorAndDestory(), e -> log.warning(e.getMessage()));
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 间隔3s后开始注册链接
                log.info("尝试建立中心连接...");
                ThreadUtil.execSilentException(() -> nodeService.acceptCenter(), e -> log.warning(e.getMessage()));
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 同时向注册中心注册地址
                log.info("注册本机节点地址...");
                ThreadUtil.execSilentException(() -> nodeService.registerNode(), e -> log.warning(e.getMessage()));
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 向注册中心同步目录
                log.info("普通节点向中心节点获取配置信息...");
                ThreadUtil.execSilentException(() -> nodeService.syncConf(), e -> log.warning(e.getMessage()));
                // 中心节点更新节点信息
                TimeUtil.execDapByFunction(() -> false, 2000, 1);
                log.info("中心节点间的节点配置信息同步...");
                ThreadUtil.execSilentException(() -> nodeService.syncCenterConf(), e -> log.warning(e.getMessage()));

                log.info("初始化完毕...");
                return false;
            }, 15000);
        });
    }

    /**
     * 启动rpc服务
     */
    public boolean rpcStart() throws Exception {
        log.info("开始启动节点服务...");
        RpcServer rpcServer = new RpcServer(nodeConfig.getLocalNodePort(), new NioEventLoopGroup(), new NioEventLoopGroup());
        rpcServer.start();
        log.info("结束启动节点服务...");
        Thread.sleep(3000);
        return true;
    }
}
