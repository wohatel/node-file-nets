package com.murong.nets.vo;

import lombok.Data;

@Data
public class UploadFileVo {

    /**
     * 文件
     */
    private String file;

    /**
     * 文件总大小
     */
    private Long totalByteSize;

    /**
     * 所在节点
     */
    private String nodeName;

}
