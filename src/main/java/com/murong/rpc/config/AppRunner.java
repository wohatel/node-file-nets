package com.murong.rpc.config;

import com.murong.rpc.util.MD5Util;
import com.murong.rpc.vo.NodeVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class AppRunner implements ApplicationRunner {

    Logger logger = LoggerFactory.getLogger(AppRunner.class);

    @Autowired
    NodeConfig nodeConfig;

    @Autowired
    HomeDirConfig homeDirConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> homeDirs = homeDirConfig.getList();
        // 安全的工作目录
        if (!CollectionUtils.isEmpty(homeDirs)) {
            EnvConfig.clearHomeDirsAndAddAll(homeDirs, System.currentTimeMillis());
        }
        logger.info("读取中心节点");
        // 设置本机节点名称
        EnvConfig.setLocalNodeName(nodeConfig.getLocalNodeName());
        // 中心节点
        List<String> list = nodeConfig.getList();
        if (!CollectionUtils.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                String ipPort = list.get(i);
                String[] split = ipPort.split(":");
                NodeVo nodeVo = new NodeVo();
                nodeVo.setPort(Integer.parseInt(split[1]));
                nodeVo.setHost(split[0]);
                nodeVo.setName(MD5Util.getMD5(ipPort).substring(0, 8));
                EnvConfig.addCenterNode(nodeVo);
            }
        }
    }
}
