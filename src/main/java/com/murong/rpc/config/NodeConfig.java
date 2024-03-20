package com.murong.rpc.config;

import com.murong.rpc.util.MD5Util;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "link.node")
@Data
public class NodeConfig {

    /**
     * node的配置节点
     */
    private List<String> list;

    /**
     * 本机的rpc服务启动的port
     */
    private int nodePort;
    /**
     * 本机的rpc服务启动的host
     */
    private String nodeHost;

    /**
     * 启动时间
     */
    private long startTime = System.currentTimeMillis();

    /**
     * 本机节点名称
     */
    private String nodeName;

    public String getLocalNodeName() {
        if (nodeName != null) {
            return nodeName;
        }
        return MD5Util.getMD5(this.nodeHost + ":" + this.nodePort).substring(0, 8);
    }

}
