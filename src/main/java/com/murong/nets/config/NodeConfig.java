package com.murong.nets.config;

import com.murong.nets.constant.NodeModelEnum;
import com.murong.nets.util.IpUtil;
import com.murong.nets.util.MD5Util;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "node.base")
@Data
public class NodeConfig {


    /**
     * node的配置节点
     */
    private List<String> mainNodes;

    /**
     * 本机的rpc服务启动的port
     */
    private int localNodePort;
    /**
     * 本机的rpc服务启动的host
     */
    private String localNodeHost = IpUtil.getIp();

    /**
     * node的节点模式,默认是simple
     */
    private NodeModelEnum localNodeModel = NodeModelEnum.simple;

    /**
     * 启动时间
     */
    private long startTime = System.currentTimeMillis();


    public String getLocalNodeName() {
        return MD5Util.getMD5(this.localNodeHost + ":" + this.localNodePort).substring(0, 8);
    }

}
