package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CpDirInput {

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
     * 源文件夹
     */
    @NotBlank
    private String sourceDir;

    /**
     * 目标文件夹
     */
    @NotBlank
    private String targetDir;

}
