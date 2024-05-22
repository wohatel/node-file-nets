package com.murong.nets.config;

import com.murong.nets.client.ClientSitePool;
import com.murong.nets.server.RpcServer;
import com.murong.nets.service.NodeService;
import com.murong.nets.util.MD5Util;
import com.murong.nets.util.ThreadUtil;
import com.murong.nets.util.TimeUtil;
import com.murong.nets.vo.NodeVo;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PoolManagerRunner implements ApplicationRunner {

    private final Logger log = LoggerFactory.getLogger(PoolManagerRunner.class);

    private final NodeConfig nodeConfig;

    private final NodeService nodeService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 检测配置中心节点
        configCenterNodes();
        // 首先启动当前节点
        rpcStart();
        // 管理本机链接远程的端口
        ExecutorPool.getExecutorService().submit(() -> { // 每15s检测下连接
            TimeUtil.execDapByFunction(() -> {
                // 每10s检测下连接池
                log.info("尝试清理无效的连接...");
                ThreadUtil.execSilentException(ClientSitePool::monitorAndDestory, e -> log.error(e.getMessage()));
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 间隔3s后开始注册链接
                log.info("尝试建立中心连接...");
                ThreadUtil.execSilentException(nodeService::acceptCenter, e -> log.error(e.getMessage()));
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 同时向注册中心注册地址
                log.info("注册本机节点地址...");
                ThreadUtil.execSilentException(nodeService::registerNode, e -> log.error(e.getMessage()));
                TimeUtil.execDapByFunction(() -> false, 3000, 1);
                // 向注册中心同步目录
                log.info("普通节点向中心节点获取配置信息...");
                ThreadUtil.execSilentException(nodeService::syncConf, e -> log.error(e.getMessage()));
                // 中心节点更新节点信息
                TimeUtil.execDapByFunction(() -> false, 2000, 1);
                log.info("中心节点间的节点配置信息同步...");
                ThreadUtil.execSilentException(nodeService::syncCenterConf, e -> log.error(e.getMessage()));
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

    /**
     * 检查配置中心节点
     */
    public void configCenterNodes() {
        // 设置本机节点名称
        EnvConfig.setLocalNodeName(nodeConfig.getLocalNodeName());
        // 中心节点
        List<String> list = nodeConfig.getMainNodes();
        if (!CollectionUtils.isEmpty(list)) {
            for (String ipPort : list) {
                String[] split = ipPort.split(":");
                NodeVo nodeVo = new NodeVo();
                nodeVo.setPort(Integer.parseInt(split[1]));
                nodeVo.setHost(split[0]);
                nodeVo.setName(MD5Util.getMD5(ipPort).substring(0, 8));
                if (nodeVo.getName().equals(nodeConfig.getLocalNodeName())) {// 本地节点是中心节点
                    nodeVo.setStartTime(nodeConfig.getStartTime());
                }
                EnvConfig.addCenterNode(nodeVo);
            }
        } else {
            String config = """
                    您没有中心节点
                    请参考下边的配置文件配置
                                        
                    # 每个节点的对外web服务
                    server.port=8081
                    # 1节点所属的类型,simple :表示普通集群节点,默认为simple,可不用配置
                    # 2节点所属的类型,bridge :表示桥接节点,链接两个独立的集群,基本很少有这么大规模的节点服务,默认以simple配置即可
                    node.base.node-model=bridge
                    # 中心节点配置,一个集群可以配置多个中心节点,中心节点维护集群节点的连接,多个中心节点可以做到高可用
                    node.base.main-nodes[0]=127.0.0.1:8000
                    # 本节点的rpc连接端口
                    node.base.local-node-port=8000
                    # 本节点的机器ip,如果此处不填写,程序会自动获取本地ip; 建议填写
                    node.base.local-node-host=192.168.10.1
                    """;
            log.error(config);
            System.exit(400);
        }
    }
}
