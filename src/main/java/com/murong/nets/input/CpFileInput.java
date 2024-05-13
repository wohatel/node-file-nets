package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CpFileInput {

    /**
     * 源节点
     */
    @NotBlank
    private String sourceNode;

    /**
     * 目标
     */
    @NotBlank
    private String targetNode;

    /**
     * 源文件
     */
    @NotBlank
    private String sourceFile;

    /**
     * 目标文件
     */
    @NotBlank
    private String targetFile;

}
