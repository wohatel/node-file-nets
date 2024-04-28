package com.murong.rpc.input;

import lombok.Data;

/**
 * 获取节点的文件信息
 */
@Data
public class GetFileOfDirInput {

    /**
     * 源节点
     */
    private String nodeName;

    /**
     * 目标
     */
    private String dir;

}
