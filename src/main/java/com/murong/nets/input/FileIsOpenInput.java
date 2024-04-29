package com.murong.nets.input;

import lombok.Data;

@Data
public class FileIsOpenInput {

    /**
     * 目标节点
     */
    private String targetNode;

    /**
     * 目标文件
     */
    private String targetFile;

}
