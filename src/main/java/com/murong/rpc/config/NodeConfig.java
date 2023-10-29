package com.murong.rpc.config;

import com.murong.rpc.util.MD5Util;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "link.node")
public class NodeConfig {

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
     * 本机几点名称
     */
    private String nodeName;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    private List<String> list;

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String getLocalNodeName() {
        if (nodeName != null) {
            return nodeName;
        }
        return MD5Util.getMD5(this.nodeHost + ":" + this.nodePort).substring(0, 8);
    }

}
