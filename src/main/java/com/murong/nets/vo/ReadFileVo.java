package com.murong.nets.vo;

import lombok.Data;

@Data
public class ReadFileVo {

    /**
     * 文件
     */
    private String file;

    /**
     * 文件总大小
     */
    private Long totalByteSize;

    /**
     * 本次读取内容
     */
    private String content;

    /**
     * next position
     */
    private Long nextPosition;

    /**
     * 错误信息
     */
    private String errorMsg;
}
