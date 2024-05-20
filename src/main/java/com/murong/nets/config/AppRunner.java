package com.murong.nets.config;

import com.murong.nets.util.MD5Util;
import com.murong.nets.vo.NodeVo;
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
public class AppRunner implements ApplicationRunner {

    Logger logger = LoggerFactory.getLogger(AppRunner.class);

    private final NodeConfig nodeConfig;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("读取中心节点");
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
        }
    }
}
