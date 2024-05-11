package com.murong.nets.input;

import lombok.Data;

@Data
public class ReadFileInput {

    /**
     * 源节点
     */
    private String nodeName;

    /**
     * 文件
     */
    private String file;

    /**
     * 本次读取的索引
     */
    private Long position;

    /**
     * 读取大小
     */
    private Long readSize;

}
