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
    private long startTime;
}
