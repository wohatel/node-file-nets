package com.murong.nets.input;

import lombok.Data;

@Data
public class FileClearOkInput {

    /**
     * 目标节点
     */
    private String targetNode;

    /**
     * 目标文件或目录
     */
    private String targetFile;

}
