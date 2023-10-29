package com.murong.rpc.vo;

import lombok.Data;

/**
 * 服务器连接的节点
 */
@Data
public class NodeVo {
    private String name;
    private int port;
    private String host;
    private boolean isActive;
    /**
     * 接地那描述
     */
    private String mark;
    private long startTime;
}
