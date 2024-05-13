package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RenameFileInput {

    /**
     * 源节点
     */
    @NotBlank
    private String nodeName;

    /**
     * 源文件
     */
    @NotBlank
    private String file;

    /**
     * 目标文件
     */
    @NotBlank
    private String newName;
}
