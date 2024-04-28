package com.murong.rpc.input;

import lombok.Data;

@Data
public class CpDirInput {

    /**
     * 源节点
     */
    private String sourceNode;

    /**
     * 目标
     */
    private String targetNode;

    /**
     * 源文件夹
     */
    private String sourceDir;

    /**
     * 目标文件夹
     */
    private String targetDir;

}
