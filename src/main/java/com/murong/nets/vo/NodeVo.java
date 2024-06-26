package com.murong.nets.vo;

import com.murong.nets.constant.NodeModelEnum;
import lombok.Data;

/**
 * 服务器连接的节点
 */
@Data
public class NodeVo {
    private String name;
    private int port;
    private String host;
    private boolean isActive = true;
    private long startTime;
    private NodeModelEnum nodeModel;
}
