package com.murong.nets.input;

import lombok.Data;

@Data
public class ClearOkFileInput {

    /**
     * 目标节点
     */
    private String nodeName;

    /**
     * 目标文件
     */
    private String file;

}
