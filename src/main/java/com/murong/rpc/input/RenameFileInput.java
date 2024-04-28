package com.murong.rpc.input;

import lombok.Data;

@Data
public class RenameFileInput {

    /**
     * 源节点
     */
    private String nodeName;

    /**
     * 源文件
     */
    private String file;

    /**
     * 目标文件
     */
    private String newName;
}
