package com.murong.rpc.config;

import com.murong.rpc.client.ClientSitePool;
import com.murong.rpc.service.NodeService;
import com.murong.rpc.util.ThreadUtil;
import com.murong.rpc.util.TimeUtil;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PoolManagerRunner implements ApplicationRunner {

    Logger logger = LoggerFactory.getLogger(PoolManagerRunner.class);

    @Autowired
    NodeConfig nodeConfig;

    @Autowired
    NodeService nodeService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
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
                logger.info("获取家目录...");
                ThreadUtil.execSilentException(() -> nodeService.syncCenterHomeDirs(), e -> e.printStackTrace());
                // 中心节点更新节点信息
                TimeUtil.execDapByFunction(() -> false, 2000, 1);
                logger.info("中心节点间的节点数据同步...");
                ThreadUtil.execSilentException(() -> nodeService.syncCenterNodes(), e -> e.printStackTrace());
                logger.info("初始化完毕...");
                return false;
            }, 60000);
        });
    }

}
